package gr.hua.dit.StudyRooms.core.service.mapper;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.service.StudySpaceBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert {@link Reservation} to {@link ReservationView}
 */
@Component
public class ReservationMapper {
    private final StudySpaceBusinessLogicService studySpaceBusinessLogicService;

    public ReservationMapper(StudySpaceBusinessLogicService studySpaceBusinessLogicService) {
        this.studySpaceBusinessLogicService = studySpaceBusinessLogicService;
    }

    public ReservationView convertReservationToReservationView(final Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        StudySpace space = reservation.getStudySpace();

        return new ReservationView(
                reservation.getId(),
                reservation.getReservationId(),
                reservation.getStudent().getLibraryId(),
                space != null ? space.getId().toString() : null,
                space != null ? space.getName() : null,
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPresent()
        );
    }
}
