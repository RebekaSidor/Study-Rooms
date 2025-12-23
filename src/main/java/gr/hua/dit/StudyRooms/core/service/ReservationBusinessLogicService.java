package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service (contract) for managing reservations.
 */
public interface
ReservationBusinessLogicService {

    CreateReservationResult createReservation(final CreateReservationRequest request, final boolean notify);

    default CreateReservationResult createReservation(final CreateReservationRequest request) {
        return this.createReservation(request, false);
    }

    boolean existsOverlappingReservation(String studySpaceId, LocalDateTime startTime, LocalDateTime endTime);
    boolean cancelReservation(Long reservationId, String libraryId);

    List<ReservationView> getAllReservations();
    List<ReservationView> getReservationsForStudentOnDate(String studentId, LocalDate date);
    List<ReservationView> getReservationsByStudentId(String studentId);
    List<ReservationView> getMyReservations(String studentId);

    long countAllReservations();
    long countActiveUsers(); // users with reservation in last 30 days

    Map<String, Long> getReservationsPerRoom();
    Map<Integer, Long> getReservationsPerHourForToday();

}
