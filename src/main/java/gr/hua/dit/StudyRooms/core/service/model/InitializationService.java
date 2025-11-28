package gr.hua.dit.StudyRooms.core.service.model;


import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.service.PersonService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 *  Initializes application
 */

@Service
public class InitializationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializationService.class);

    private final PersonService personService;
    private final AtomicBoolean initialized;

    public InitializationService(final PersonService personService) {
        if (personService == null) throw new NullPointerException();
        this.personService = personService;
        this.initialized = new AtomicBoolean(false);
    }

    @PostConstruct
    public void populateDatabase(){

        final boolean alreadyIntialized = this.initialized.getAndSet(true);
        if (alreadyIntialized){
            LOGGER.warn("Database initialization skipped: initial data has already been populated");
            return;

        }
        LOGGER.info("Starting database initialization with initial data..");
        final List<CreatePersonRequest> createPersonRequestList = List.of(
                new CreatePersonRequest(
                        PersonType.LIB_STAFF,
                        "lib2025002",
                        "ananas",
                        "karpouzakis",
                        "ananoulis@lib.gr",
                        "690000000000",
                        "1234"
                ),
                new CreatePersonRequest(
                        PersonType.STUDENT,
                        "lib2025001",
                        "olga",
                        "fasmoulou",
                        "olgaolga@lib.gr",
                        "6981062342",
                        "1234"
                )
        );
        for (final var CreatePersonRequest : createPersonRequestList) {
            this.personService.createPerson(CreatePersonRequest);
        }
        LOGGER.info("Database initialization completed successfully");

    }

}
