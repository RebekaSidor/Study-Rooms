package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import java.util.List;

/**
 * Service for managing {@code Reservation} for data analytics purposes.
 */
public interface ReservationDataService {

    List<ReservationView> getAllReservations();

    ReservationView createReservation(CreateReservationRequest request);

    void cancelReservation(Long reservationId);

    List<ReservationView> getMyReservations();
}
