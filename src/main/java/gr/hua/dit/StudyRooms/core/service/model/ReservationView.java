package gr.hua.dit.StudyRooms.core.service.model;

import java.time.LocalDateTime;

/**
 * ReservationView (DTO)
 */
public record ReservationView(
        long id,
        String reservationId,
        String studentId,
        String studySpaceId,
        String studySpaceName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Boolean present
) {}
