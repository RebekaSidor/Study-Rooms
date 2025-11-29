package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;

public interface ReservationService {
    CreateReservationResult createReservation(final CreateReservationRequest request, final boolean notify);

    default CreateReservationResult createReservation(final CreateReservationRequest createReservationRequest) {
        return this.createReservation(createReservationRequest);
    }
}
