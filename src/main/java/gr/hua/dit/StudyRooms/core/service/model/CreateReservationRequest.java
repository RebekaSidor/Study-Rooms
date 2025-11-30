package gr.hua.dit.StudyRooms.core.service.model;

/**
 * CreateReservationRequest (DTO).
 */
public record CreateReservationRequest(
        String reservationId,
        String studentId,
        String studySpaceId,
        java.time.LocalTime timeslot
)
{}
