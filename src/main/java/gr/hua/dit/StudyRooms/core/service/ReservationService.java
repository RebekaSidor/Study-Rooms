package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService {
    CreateReservationResult createReservation(final CreateReservationRequest request, final boolean notify);

    default CreateReservationResult createReservation(final CreateReservationRequest request) {
        return this.createReservation(request, false); // call the other method
    }
    List<Reservation> getReservationsForStudySpace(String studySpaceId);

    List<Reservation> getReservationsForStudent(String studentId);

    boolean existsOverlappingReservation(String studySpaceId,
                                         LocalDateTime startTime,
                                         LocalDateTime endTime);

}
