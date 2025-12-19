package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReservationService {
    CreateReservationResult createReservation(final CreateReservationRequest request, final boolean notify);

    default CreateReservationResult createReservation(final CreateReservationRequest request) {
        return this.createReservation(request, false);
    }

    boolean existsOverlappingReservation(String studySpaceId, LocalDateTime startTime, LocalDateTime endTime);

    List<ReservationView> getAllReservations();

    long countAllReservations();
    long countActiveUsers();// users with reservation in last 30 days
    Map<String, Long> getReservationsPerRoom();
    List<ReservationView> getReservationsByStudentId(String studentId);
    long getFullyBookedRoomsToday();

    List<ReservationView> getReservationsForStudentView(String studentId);
    Map<Integer, Long> getReservationsPerHourForToday();

    boolean cancelReservation(Long reservationId, String libraryId);

    List<ReservationView> getReservationsForStudentOnDate(String studentId, LocalDate date);

    boolean studentHasOverlappingReservation(String studentId, LocalDateTime startTime, LocalDateTime endTime);

    void markAttendance(Long reservationId, boolean present);
}
