package gr.hua.dit.StudyRooms.core.service;
import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  Initializes application
 */

@Service
public class InitializationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializationService.class);

    private final PersonService personService;
    private final StudySpaceService studySpaceService;
    private final ReservationService reservationService;
    private final AtomicBoolean initialized;

    public InitializationService(final PersonService personService, final StudySpaceService studySpaceService, final ReservationService reservationService) {
        if (personService == null) throw new NullPointerException();
        if (studySpaceService == null) throw new NullPointerException();
        if (reservationService == null) throw new NullPointerException();
        this.personService = personService;
        this.studySpaceService = studySpaceService;
        this.reservationService = reservationService;
        this.initialized = new AtomicBoolean(false);
    }

    @PostConstruct
    public void populateDatabase(){

        final boolean alreadyIntialized = this.initialized.getAndSet(true);
        if (alreadyIntialized){
            LOGGER.warn("Database initialization skipped: initial data has already been populated");
            return;

        }

        // INITIALIZE PERSONS
        LOGGER.info("Starting database initialization with initial data..");
        final List<CreatePersonRequest> createPersonRequestList = List.of(
                new CreatePersonRequest(
                        PersonType.STUDENT,
                        "lib2025001",
                        "ananas",
                        "karpouzakis",
                        "ananoulis@lib.gr",
                        "690000000000",
                        "1234"
                ),
                new CreatePersonRequest(
                        PersonType.STUDENT,
                        "lib2025002",
                        "olga",
                        "fasmoulou",
                        "olgaolga@lib.gr",
                        "6981062342",
                        "1234"
                ),
                new CreatePersonRequest(
                        PersonType.STUDENT,
                        "lib2025002",
                        "rebeka",
                        "sidor",
                        "rev@lib.gr",
                        "6955000000",
                        "1234"
                ),
                new CreatePersonRequest(
                        PersonType.LIB_STAFF,
                        "st2025001",
                        "emi",
                        "spahi",
                        "emi@lib.gr",
                        "690000000000",
                        "1234"
                ),
                new CreatePersonRequest(
                        PersonType.LIB_STAFF,
                        "st2025002",
                        "Spongebob",
                        "Squarepants",
                        "bob@lib.gr",
                        "690000000000",
                        "1234"
                ),
                new CreatePersonRequest(
                        PersonType.LIB_STAFF,
                        "st2025003",
                        "Darth",
                        "Vader",
                        "dv@lib.gr",
                        "690000000000",
                        "1234"
                )
        );
        for (final var CreatePersonRequest : createPersonRequestList) {
            this.personService.createPerson(CreatePersonRequest, false);//do not send sms
        }

        // INITIALIZE STUDY SPACES
        final List<CreateStudySpaceRequest> createStudySpaceRequestList = List.of(
                new CreateStudySpaceRequest(
                        StudySpaceType.ROOM,
                        "r001",
                        "R1",
                        6,
                        true,
                        LocalTime.of(8, 0),
                        LocalTime.of(20, 0)
                ),
                new CreateStudySpaceRequest(
                        StudySpaceType.ROOM,
                        "r002",
                        "R2",
                        5,
                        true,
                        LocalTime.of(8, 0),
                        LocalTime.of(20, 0)
                ),
                new CreateStudySpaceRequest(
                        StudySpaceType.ROOM,
                        "r003",
                        "R3",
                        8,
                        true,
                        LocalTime.of(8, 0),
                        LocalTime.of(20, 0)
                ),
                new CreateStudySpaceRequest(
                        StudySpaceType.SEAT,
                        "s001",
                        "S1",
                        null,
                        true,
                        LocalTime.of(8, 0),
                        LocalTime.of(20, 0)
                ),
                new CreateStudySpaceRequest(
                        StudySpaceType.SEAT,
                        "s002",
                        "S2",
                        null,
                        true,
                        LocalTime.of(8, 0),
                        LocalTime.of(20, 0)
                ),
                new CreateStudySpaceRequest(
                        StudySpaceType.SEAT,
                        "s003",
                        "S3",
                        null,
                        true,
                        LocalTime.of(8, 0),
                        LocalTime.of(20, 0)
                )
        );
        for (final var CreateStudySpaceRequest : createStudySpaceRequestList) {
            this.studySpaceService.createStudySpace(CreateStudySpaceRequest);
        }

        // INITIALIZE RESERVATIONS
        final List<CreateReservationRequest> CreateReservationRequestlist = List.of(
                new CreateReservationRequest(
                        "r2025001",
                        "lib2025002",
                        "s003",
                        LocalDateTime.now()
                ),
                new CreateReservationRequest(
                        "r2025002",
                        "lib2025002",
                        "r003",
                        LocalDateTime.now()
                )
        );
        for (final var CreateReservationRequest : CreateReservationRequestlist) {
            this.reservationService.createReservation(CreateReservationRequest);
        }

        LOGGER.info("Database initialization completed successfully");
    }
}
