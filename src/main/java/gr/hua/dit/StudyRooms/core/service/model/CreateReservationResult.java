package gr.hua.dit.StudyRooms.core.service.model;

/**
 * CreateReservationResult (DTO).
 */
public record CreateReservationResult(boolean created, String reason, ReservationView reservationView) {
    public static CreateReservationResult success(final ReservationView reservationView) {
        if (reservationView == null) throw new NullPointerException();
        return new CreateReservationResult(true, null, reservationView);
    }

    public static CreateReservationResult fail(final String reason) {
        if (reason == null) throw new NullPointerException();
        if (reason.isBlank()) throw new IllegalArgumentException();
        return new CreateReservationResult(false, reason, null);
    }
}
