package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ReservationService {
    CreateReservationResult createReservation(final CreateReservationRequest request, final boolean notify);

    default CreateReservationResult createReservation(final CreateReservationRequest request) {
        return this.createReservation(request, false); // call the other method
    }

    List<Reservation> getReservationsForStudySpace(String studySpaceId);
    List<Reservation> getReservationsForStudent(String studentId);

    boolean existsOverlappingReservation(String studySpaceId, LocalDateTime startTime, LocalDateTime endTime);

    void deleteReservation(long id);

    List<Reservation> getUpcomingReservations(String studentId);
    List<Reservation> getPastReservations(String studentId);
    List<ReservationView> getAllReservations();

    long countAllReservations();
    long countActiveUsers();   // users with reservation in last 30 days
    Map<String, Long> getReservationsPerRoom();
    List<ReservationView> getReservationsByStudentId(String studentId);
    long getFullyBookedRoomsToday();

    List<ReservationView> getReservationsForStudentView(String studentId);
}
