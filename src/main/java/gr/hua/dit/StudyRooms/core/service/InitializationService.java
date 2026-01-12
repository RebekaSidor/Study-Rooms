package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.model.Client;
import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.repository.ClientRepository;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Populates the database with initial study spaces.
 */
@Service
public class InitializationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializationService.class);

    private final ClientRepository clientRepository;
    private final StudySpaceBusinessLogicService studySpaceBusinessLogicService;
    private final PersonBusinessLogicService personBusinessLogicService;
    private final ReservationBusinessLogicService reservationBusinessLogicService;
    private final AtomicBoolean initialized;
    private final PasswordEncoder passwordEncoder;

    public InitializationService(
            final ClientRepository clientRepository,
            PersonBusinessLogicService personBusinessLogicService,
            StudySpaceBusinessLogicService studySpaceBusinessLogicService,
            ReservationBusinessLogicService reservationBusinessLogicService,
            PasswordEncoder passwordEncoder
    ) {
        if (clientRepository == null) throw new NullPointerException();
        if (personBusinessLogicService == null) throw new NullPointerException();
        if (studySpaceBusinessLogicService == null) throw new NullPointerException();
        if (reservationBusinessLogicService == null) throw new NullPointerException();
        this.clientRepository = clientRepository;
        this.personBusinessLogicService = personBusinessLogicService;
        this.studySpaceBusinessLogicService = studySpaceBusinessLogicService;
        this.reservationBusinessLogicService = reservationBusinessLogicService;
        this.initialized = new AtomicBoolean(false);
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void populateDatabase() {
        boolean alreadyInitialized = initialized.getAndSet(true);
        if (alreadyInitialized) {
            LOGGER.warn("Database initialization skipped: already initialized.");
            return;
        }

        LOGGER.info("Starting database initialization...");

        /* ---------- CLIENTS ---------- */
        if (clientRepository.count() == 0) {
            LOGGER.info("Creating initial clients...");

            List<Client> clients = List.of(
                    new Client(
                            null,
                            "client01",
                            passwordEncoder.encode("s3cr3t"),
                            "INTEGRATION_READ,INTEGRATION_WRITE"
                    ),
                    new Client(
                            null,
                            "client02",
                            passwordEncoder.encode("s3cr3t"),
                            "INTEGRATION_READ"
                    )
            );

            clientRepository.saveAll(clients);
        } else {
            LOGGER.info("Clients already exist — skipping client initialization.");
        }

        /* ---------- STUDY SPACES ---------- */
        long studySpaceCount = studySpaceBusinessLogicService.countAll();
        if (studySpaceCount > 0) {
            LOGGER.info("Study spaces already exist — skipping study space initialization.");
            return;
        }

        LOGGER.info("Creating initial study spaces...");


        //create initial StudySpaces ---
        List<CreateStudySpaceRequest> spaces = List.of(
                new CreateStudySpaceRequest(StudySpaceType.ROOM, "r001", "R1", 6, true,
                        LocalTime.of(8, 0), LocalTime.of(21, 0)),
                new CreateStudySpaceRequest(StudySpaceType.ROOM, "r002", "R2", 5, true,
                        LocalTime.of(8, 0), LocalTime.of(21, 0)),
                new CreateStudySpaceRequest(StudySpaceType.ROOM, "r003", "R3", 8, true,
                        LocalTime.of(8, 0), LocalTime.of(21, 0)),
                new CreateStudySpaceRequest(StudySpaceType.SEAT, "s001", "S1", null, true,
                        LocalTime.of(8, 0), LocalTime.of(22, 0)),
                new CreateStudySpaceRequest(StudySpaceType.SEAT, "s002", "S2", null, true,
                        LocalTime.of(8, 0), LocalTime.of(22, 0)),
                new CreateStudySpaceRequest(StudySpaceType.SEAT, "s003", "S3", null, true,
                        LocalTime.of(8, 0), LocalTime.of(22, 0))
        );

        for (CreateStudySpaceRequest req : spaces) {
            studySpaceBusinessLogicService.createStudySpace(req);
        }

        LOGGER.info("Study spaces initialization completed successfully");

        SecurityContextHolder.clearContext();
    }
}
