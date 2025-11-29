package gr.hua.dit.StudyRooms.core.service.model;

/**
 * ReservationView (DTO) that includes only inform to be exposed.
 */
public record ReservationView(
        long id,
        String reservationId,
        String studentId,
        String studySpaceId
)
{}
