package gr.hua.dit.StudyRooms.core.service.model;
import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import java.time.LocalTime;

/**
 * StudySpaceView (DTO) that includes only inform to be exposed.
 */
public record StudySpaceView(
        long id,
        StudySpaceType type,
        String studySpaceId,
        String name,
        Integer capacity,
        Boolean available,
        LocalTime openingTime,
        LocalTime closingTime)
{}
