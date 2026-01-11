package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.Person;
import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.port.PhoneNumberPort;
import gr.hua.dit.StudyRooms.core.port.SmsNotificationPort;
import gr.hua.dit.StudyRooms.core.port.impl.dto.PhoneNumberValidationResult;
import gr.hua.dit.StudyRooms.core.repository.PersonRepository;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import gr.hua.dit.StudyRooms.core.service.PersonBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.mapper.PersonMapper;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonResult;
import gr.hua.dit.StudyRooms.core.service.model.PersonView;
import gr.hua.dit.StudyRooms.core.service.model.StudentStatus;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link PersonBusinessLogicService}.
 */
@Service
public class PersonBusinessLogicServiceImpl implements PersonBusinessLogicService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonBusinessLogicServiceImpl.class);

    private final Validator validator;
    private  final PasswordEncoder passwordEncoder;
    private final PersonRepository personRepository;
    private final ReservationRepository reservationRepository;
    private final PersonMapper personMapper;
    private final PhoneNumberPort phoneNumberPort;
    private final SmsNotificationPort smsNotificationPort;

    public PersonBusinessLogicServiceImpl(final Validator validator,
                                          final PasswordEncoder passwordEncoder,
                                          final PersonRepository personRepository,
                                          final ReservationRepository reservationRepository,
                                          final PersonMapper personMapper,
                                          final PhoneNumberPort phoneNumberPort,
                                          final SmsNotificationPort smsNotificationPort) {
        if (validator == null) throw new NullPointerException();
        if (passwordEncoder == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (reservationRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();
        if (phoneNumberPort == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();

        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.personRepository = personRepository;
        this.reservationRepository = reservationRepository;
        this.personMapper = personMapper;
        this.phoneNumberPort = phoneNumberPort;
        this.smsNotificationPort = smsNotificationPort;
    }

    //create new person
    @Transactional
    @Override
    public CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest, final boolean notify) {
        if (createPersonRequest == null) throw new NullPointerException();

        //CreatePersonRequest validation.
        final Set<ConstraintViolation<CreatePersonRequest>> requestViolations
                = this.validator.validate(createPersonRequest);
        if (!requestViolations.isEmpty()) {
            final StringBuilder sb = new StringBuilder();
            for (final ConstraintViolation<CreatePersonRequest> violation : requestViolations) {
                sb
                        .append(violation.getPropertyPath())
                        .append(": ")
                        .append(violation.getMessage())
                        .append("\n");
            }
            return CreatePersonResult.fail(sb.toString());
        }

        // Unpack (we assume valid `CreatePersonRequest` instance)
        final PersonType type = createPersonRequest.type();
        final String firstName = createPersonRequest.firstName().strip();
        final String lastName = createPersonRequest.lastName().strip();
        final String emailAddress =  createPersonRequest.emailAddress().strip();
        String mobilePhoneNumber = createPersonRequest.mobilePhoneNumber().strip();
        final String rawPassword = createPersonRequest.rawPassword();

        LOGGER.info("Creating person: {} {}, type={}", firstName, lastName, type);
        LOGGER.info("Email: {}, Phone: {}", emailAddress, mobilePhoneNumber);

        // Advanced mobile phone number validation.
        final PhoneNumberValidationResult phoneNumberValidationResult
                = this.phoneNumberPort.validate(mobilePhoneNumber);
        if (!phoneNumberValidationResult.isValidMobile()) {
            LOGGER.warn("Invalid mobile number: {}", mobilePhoneNumber);
            return CreatePersonResult.fail("Mobile Phone Number is not valid");
        }
        mobilePhoneNumber = phoneNumberValidationResult.e164();

        //validation
        if (this.personRepository.existsByEmailAddressIgnoreCase(emailAddress)) {
            LOGGER.warn("Email already exists: {}", emailAddress);
            return CreatePersonResult.fail("Email address must be unique");
        }
        if (this.personRepository.existsByMobilePhoneNumber(mobilePhoneNumber)) {
            LOGGER.warn("Phone already exists: {}", mobilePhoneNumber);
            return CreatePersonResult.fail("Mobile Phone number must be unique");
        }

        //generate library id for each person type
        final String libraryId;
        if (type == PersonType.STUDENT) {
            libraryId = generateNextStudentId(); //(lib2025xxx)
        } else if (type == PersonType.LIB_STAFF) {
            libraryId = generateNextStaffId();   //(s0001, s0002...)
        } else {
            throw new IllegalArgumentException("Unsupported person type: " + type);
        }

        //encode password
        final String hashedPassword = this.passwordEncoder.encode(rawPassword);

        // Instantiate person.
        Person person = new Person();
        person.setId(null); //auto generated
        person.setLibraryId(libraryId);
        person.setType(type);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(emailAddress);
        person.setMobilePhoneNumber(mobilePhoneNumber);
        person.setPasswordHash(hashedPassword);
        person.setCreatedAt(null); // auto generated
        // --------------------------------------------------

        final Set<ConstraintViolation<Person>> personViolations = this.validator.validate(person);
        if (!personViolations.isEmpty()) {
            throw new RuntimeException("invalid Person instance"); //indicate a programmer error, not a client error
        }

        //save/insert to database
        person = this.personRepository.save(person);
        LOGGER.info("Person saved with ID: {}", person.getLibraryId());
        // --------------------------------------------------

        // Send SMS notification
        if (notify) {
            final String content = String.format(
                    "Registration successful! Your Library ID is %s. Use it for log-in!",
                    libraryId, emailAddress);
            final boolean sent = this.smsNotificationPort.sendSms(mobilePhoneNumber, content);
            if (!sent) {
                LOGGER.warn("SMS send to {} failed!", mobilePhoneNumber);
            }
        }

        //convert to view
        final PersonView personView = this.personMapper.convertPersonToPersonView(person);
        return CreatePersonResult.success(personView);
    }

    //id generation based on person type
    private String generateNextStudentId() {
        String prefix = "lib";
        Person last = personRepository.findTopStudentByLibraryIdStartingWithOrderByLibraryIdDesc(prefix);
        if (last == null) return prefix + "2025001"; //first ID

        String oldId = last.getLibraryId(); //"lib2025003"
        int num = Integer.parseInt(oldId.substring(prefix.length()));
        num++;
        return prefix + String.format("%07d", num);
    }
    private String generateNextStaffId() {
        String prefix = "s";
        Person last = personRepository.findTopStaffByLibraryIdStartingWithOrderByLibraryIdDesc(prefix);
        if (last == null) return prefix + "0001"; //first ID

        String oldId = last.getLibraryId(); //"s0004"
        int num = Integer.parseInt(oldId.substring(prefix.length()));
        num++;
        return prefix + String.format("%04d", num);
    }

    @Override
    public Person getPersonById(String libraryId) {
        return personRepository.findByLibraryId(libraryId).orElse(null);
    }

    /*change personal information*/
    @Override
    public String updateEmail(String libraryId, String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            return "Email cannot be empty.";
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!newEmail.matches(emailRegex)) {
            return "Invalid email format.";
        }
        if (personRepository.existsByEmailAddressIgnoreCase(newEmail)) {
            return "This email is already used.";
        }
        Person person = personRepository.findByLibraryId(libraryId).orElse(null);
        if (person == null) {
            return "User not found.";
        }
        person.setEmailAddress(newEmail);
        personRepository.save(person);

        return null;
    }

    @Override
    public String updatePhone(String libraryId, String newPhone) {
        if (newPhone == null || newPhone.isBlank()) {
            return "Phone number cannot be empty.";
        }

        // validate using external service
        PhoneNumberValidationResult validationResult = phoneNumberPort.validate(newPhone);
        if (!validationResult.isValidMobile()) {
            return "Mobile Phone Number is not valid";
        }

        String formattedPhone = validationResult.e164();

        Person person = personRepository.findByLibraryId(libraryId).orElse(null);
        if (person == null) {
            return "User not found.";
        }

        if (personRepository.existsByMobilePhoneNumber(formattedPhone)) {
            return "This phone number already belongs to another user.";
        }

        person.setMobilePhoneNumber(formattedPhone);
        personRepository.save(person);

        return null;
    }

    @Override
    public String updatePassword(String libraryId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return "Password cannot be empty.";
        }
        if (newPassword.length() < 4) {
            return "Password must be at least 4 characters.";
        }
        Person person = personRepository.findByLibraryId(libraryId).orElse(null);
        if (person == null) {
            return "User not found.";
        }
        person.setPasswordHash(passwordEncoder.encode(newPassword));
        personRepository.save(person);

        return null;
    }

    @Override
    public StudentStatus calculateStudentStatus(Person student) {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> reservations = reservationRepository.findByStudent(student);

        LocalDateTime absenceStart = student.getLastPenaltyAt() != null
                ? student.getLastPenaltyAt()
                : LocalDateTime.MIN;

        long absences = reservations.stream()
                .filter(r -> r.getEndTime() != null)
                .filter(r -> r.getEndTime().isBefore(now))
                .filter(r -> r.getEndTime().isAfter(absenceStart))
                .filter(r -> r.getPresent() == null || !r.getPresent())
                .count();

        boolean hasPenalty = student.getPenaltyUntil() != null && now.isBefore(student.getPenaltyUntil());

        if (absences >= 3 && !hasPenalty) {
            student.setPenaltyUntil(now.plusHours(1));
            student.setLastPenaltyAt(now);
            personRepository.save(student);
            hasPenalty = true;
            absences = 3;

            //SMS notification for penalty
            String content = String.format(
                    "You have received a penalty! You cannot reserve a study room until %s.",
                    student.getPenaltyUntil()
            );
            boolean sent = smsNotificationPort.sendSms(student.getMobilePhoneNumber(), content);
            if (!sent) {
                LOGGER.warn("SMS notification for penalty to {} failed!", student.getMobilePhoneNumber());
            }

            hasPenalty = true;
            absences = 3;
        }

        return new StudentStatus(absences, hasPenalty, student.getPenaltyUntil());
    }
}