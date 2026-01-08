package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import java.util.List;

/**
 * Populates the database with initial study spaces.
 */
@Service
public class InitializationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializationService.class);

    private final StudySpaceBusinessLogicService studySpaceBusinessLogicService;
    private final PersonBusinessLogicService personBusinessLogicService;
    private final ReservationBusinessLogicService reservationBusinessLogicService;

    public InitializationService(
            PersonBusinessLogicService personBusinessLogicService,
            StudySpaceBusinessLogicService studySpaceBusinessLogicService,
            ReservationBusinessLogicService reservationBusinessLogicService
    ) {
        this.personBusinessLogicService = personBusinessLogicService;
        this.studySpaceBusinessLogicService = studySpaceBusinessLogicService;
        this.reservationBusinessLogicService = reservationBusinessLogicService;
    }

    @PostConstruct
    public void populateDatabase() {
        long count = studySpaceBusinessLogicService.countAll();
        if (count > 0) {
            LOGGER.info("Database already initialized — skipping initial data load.");
            return;
        }

        LOGGER.info("Database empty — populating initial study spaces...");

        // --- Βάζουμε system user στο SecurityContext ---
        var systemUserDetails = new ApplicationUserDetails(
                0L,          // personId
                "SYSTEM",            // libraryId
                "system@init",       // email
                PersonType.LIB_STAFF,// type
                "SYSTEM"             // password placeholder
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(systemUserDetails, null, systemUserDetails.getAuthorities())
        );

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

        LOGGER.info("Study spaces created successfully!");

        SecurityContextHolder.clearContext();
    }
}
