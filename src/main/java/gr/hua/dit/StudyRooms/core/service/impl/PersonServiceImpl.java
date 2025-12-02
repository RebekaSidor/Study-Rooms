package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.Person;
import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.port.SmsNotificationPort;
import gr.hua.dit.StudyRooms.core.repository.PersonRepository;
import gr.hua.dit.StudyRooms.core.service.PersonService;
import gr.hua.dit.StudyRooms.core.service.mapper.PersonMapper;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonResult;
import gr.hua.dit.StudyRooms.core.service.model.PersonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link PersonService}.
 */
@Service
public class PersonServiceImpl implements PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

    private  final PasswordEncoder passwordEncoder;
    private final SmsNotificationPort smsNotificationPort;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonServiceImpl(final PasswordEncoder passwordEncoder,
                             final SmsNotificationPort smsNotificationPort,
                             final PersonRepository personRepository,
                             final PersonMapper personMapper) {
        if (passwordEncoder == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();

        this.passwordEncoder = passwordEncoder;
        this.smsNotificationPort = smsNotificationPort;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
    }

    /** AUTO-GENERATE LIBRARY ID */
    private String generateNextLibraryId() {
        Person last = personRepository.findTopByOrderByLibraryIdDesc();

        if (last == null || last.getLibraryId() == null) {
            return "lib2025000";
        }

        String oldId = last.getLibraryId(); // Example: lib2025003
        int num = Integer.parseInt(oldId.substring(3)); // → 2025003
        num++; // → 2025004

        return "lib" + num;
    }


    @Override
    public CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest, final boolean notify) {
        if (createPersonRequest == null) throw new NullPointerException();

        //Unpack (we assume validated CreatePersonRequest)
        // -----------------------------------------------

        final PersonType type = createPersonRequest.type();
        final String firstName = createPersonRequest.firstName().strip();
        final String lastName = createPersonRequest.lastName().strip();
        final String emailAddress =  createPersonRequest.emailAddress().strip();
        final String mobilePhoneNumber = createPersonRequest.mobilePhoneNumber().strip();
        final String rawPassword = createPersonRequest.rawPassword();


        // -------- VALIDATION --------
        if (this.personRepository.existsByEmailAddressIgnoreCase(emailAddress)){
            return CreatePersonResult.fail("Email address must be unique");
        }

        if (this.personRepository.existsByMobilePhoneNumber(mobilePhoneNumber)){
            return CreatePersonResult.fail("Mobile Phone number must be unique");
        }


        // GENERATE AUTOMATIC LIBRARY ID
        final String libraryId = generateNextLibraryId();

        // -------- ENCODE PASSWORD --------
        final String hashedPassword = this.passwordEncoder.encode(rawPassword);


        // -------- CREATE PERSON OBJECT --------
        Person person = new Person();
        person.setId(null); // auto generated
        person.setLibraryId(libraryId);
        person.setType(type);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setEmailAddress(emailAddress);
        person.setMobilePhoneNumber(mobilePhoneNumber);
        person.setPasswordHash(hashedPassword);
        person.setCreatedAt(null); // auto generated

        // -------- SAVE PERSON --------
        person = this.personRepository.save(person);


        // SMS disabled for local development
//
//        // -------- SEND SMS IF REQUESTED --------
//        if (notify){
//            final String content = String.format("You have successfully registered for Study Rooms application."+
//                    "Use your email (%s) to log in ", emailAddress);//todo message
//            final boolean sent = this.smsNotificationPort.sendSms(mobilePhoneNumber, content);
//            if (!sent) {
//                LOGGER.warn("SMS sent to {} failed", mobilePhoneNumber);
//            }
//        }

        // -------- CONVERT TO VIEW --------
        final PersonView personView = this.personMapper.convertPersonToPersonView(person);

        return CreatePersonResult.success(personView);
    }
}
