package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.Person;
import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.port.LookupPort;
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
import org.springframework.util.DigestUtils;

import java.util.List;

/**
 * Default implementation of {@link PersonService}.
 */
@Service
public class PersonServiceImpl implements PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

    private  final PasswordEncoder passwordEncoder;
    private final LookupPort lookupPort;
    private final SmsNotificationPort smsNotificationPort;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonServiceImpl(final PasswordEncoder passwordEncoder,
                             final LookupPort lookupPort,
                             final SmsNotificationPort smsNotificationPort,
                             final PersonRepository personRepository,
                             final PersonMapper personMapper) {
        if (passwordEncoder == null) throw new NullPointerException();
        if (lookupPort == null) throw new NullPointerException();
        if (smsNotificationPort == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (personMapper == null) throw new NullPointerException();

        this.passwordEncoder = passwordEncoder;
        this.lookupPort = lookupPort;
        this.smsNotificationPort = smsNotificationPort;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
    }

    @Override
    public CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest, final boolean notify) {
        if (createPersonRequest == null) throw new NullPointerException();

        //Unpack (we assume validated CreatePersonRequest)
        // -----------------------------------------------

        final PersonType type = createPersonRequest.type();
        final String libraryId = createPersonRequest.libraryId().strip(); //remove whitespaces
        final String firstName = createPersonRequest.firstName().strip();
        final String lastName = createPersonRequest.lastName().strip();
        final String emailAddress =  createPersonRequest.emailAddress().strip();
        final String mobilePhoneNumber = createPersonRequest.mobilePhoneNumber().strip();
        final String rawPassword = createPersonRequest.rawPassword();



        //Basic email address validation.
        // ---------------------------------

        if (!emailAddress.endsWith("@lib.gr")) {
            return CreatePersonResult.fail("Only library email address (@lib.gr) is supported");
        }

        // ---------------------------------

        if (this.personRepository.existsByLibraryIdIgnoreCase(libraryId)){
            return CreatePersonResult.fail("Library ID must be unique");
        }

        if (this.personRepository.existsByEmailAddressIgnoreCase(emailAddress)){
            return CreatePersonResult.fail("Email address must be unique");
        }

        if (this.personRepository.existsByMobilePhoneNumber(mobilePhoneNumber)){
            return CreatePersonResult.fail("Mobile Phone number must be unique");
        }

        // ---------------------------------


        final PersonType personType$Lookup =this.lookupPort.lookup(libraryId).orElse(null);
        if (personType$Lookup == null){
            return CreatePersonResult.fail("Invalid Library ID");
        }
        if (personType$Lookup != type){
            return CreatePersonResult.fail("The provided person type does not match the actual one");
        }

        // ---------------------------------

        // TODO encode password! raw to hash!
        final String hashedPassword = this.passwordEncoder.encode(rawPassword);

        // Instantiate person.
        // ---------------------------------

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

        // Persist person (save/insert to database)
        // ---------------------------------

        person = this.personRepository.save(person);

        //------------------------------------

        if (notify){
            final String content = String.format("You have successfully registered for Study Rooms application."+
                    "Use your email (%s) to log in ", emailAddress);//todo message
            final boolean sent = this.smsNotificationPort.sendSms(mobilePhoneNumber, content);
            if (!sent) {
                LOGGER.warn("SMS sent to {} failed", mobilePhoneNumber);
            }
        }

        // Map 'Person' to 'PersonView'
        // ---------------------------------

        final PersonView personView = this.personMapper.convertPersonToPersonView(person);

        // ---------------------------------

        return CreatePersonResult.success(personView);
    }
}
