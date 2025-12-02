package gr.hua.dit.StudyRooms.core.service.model;

import java.time.LocalDateTime;

/**
 * CreateReservationRequest (DTO).
 */
public record CreateReservationRequest(
        String reservationId,
        String studentId,
        String studySpaceId,
        LocalDateTime startTime,
        LocalDateTime endTime


) {}
