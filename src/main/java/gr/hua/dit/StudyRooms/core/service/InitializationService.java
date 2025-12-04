package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import java.util.List;

@Service
public class InitializationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializationService.class);

    private final PersonService personService;
    private final StudySpaceService studySpaceService;
    private final ReservationService reservationService;

    public InitializationService(
            PersonService personService,
            StudySpaceService studySpaceService,
            ReservationService reservationService
    ) {
        this.personService = personService;
        this.studySpaceService = studySpaceService;
        this.reservationService = reservationService;
    }

    @PostConstruct
    public void populateDatabase() {

        long count = studySpaceService.countAll();

        if (count > 0) {
            LOGGER.info("Database already initialized — skipping initial data load.");
            return;
        }

        LOGGER.info("Database empty — populating initial study spaces...");

        List<CreateStudySpaceRequest> spaces = List.of(
                new CreateStudySpaceRequest(
                        StudySpaceType.ROOM, "r001", "R1", 6, true,
                        LocalTime.of(8, 0), LocalTime.of(20, 0)
                ),
                new CreateStudySpaceRequest(
                        StudySpaceType.ROOM, "r002", "R2", 5, true,
                        LocalTime.of(8, 0), LocalTime.of(20, 0)
                ),
                new CreateStudySpaceRequest(
                        StudySpaceType.ROOM, "r003", "R3", 8, true,
                        LocalTime.of(8, 0), LocalTime.of(20, 0)
                ),
                new CreateStudySpaceRequest(
                        StudySpaceType.SEAT, "s001", "S1", null, true,
                        LocalTime.of(8, 0), LocalTime.of(20, 0)
                ),
                new CreateStudySpaceRequest(
                        StudySpaceType.SEAT, "s002", "S2", null, true,
                        LocalTime.of(8, 0), LocalTime.of(20, 0)
                ),
                new CreateStudySpaceRequest(
                        StudySpaceType.SEAT, "s003", "S3", null, true,
                        LocalTime.of(8, 0), LocalTime.of(20, 0)
                )
        );

        for (CreateStudySpaceRequest req : spaces) {
            studySpaceService.createStudySpace(req);
        }

        LOGGER.info("Study spaces created successfully!");
    }
}
