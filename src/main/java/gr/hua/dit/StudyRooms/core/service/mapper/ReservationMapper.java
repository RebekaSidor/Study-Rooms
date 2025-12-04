package gr.hua.dit.StudyRooms.core.service.mapper;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert {@link Reservation} to {@link ReservationView}
 */
@Component
public class ReservationMapper {
    /**
     * Μετατρέπει ένα Reservation entity σε ReservationView για το UI.
     */
    public ReservationView convertReservationToReservationView(final Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        return new ReservationView(
                reservation.getId(),
                reservation.getReservationId(),
                reservation.getStudentId(),
                reservation.getStudySpaceId(),
                reservation.getStartTime(),
                reservation.getEndTime()
        );

    }

}