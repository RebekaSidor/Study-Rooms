package gr.hua.dit.StudyRooms.core.service.model;

import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import java.time.LocalTime;

/**
 * CreateStudySpaceRequest (DTO).
 */
public record CreateStudySpaceRequest(
        StudySpaceType type,
        String studySpaceId,
        String name,
        Integer capacity,
        Boolean available,
        LocalTime openingTime,
        LocalTime closingTime
)
{}
