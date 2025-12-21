package gr.hua.dit.StudyRooms.core.service.mapper;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert {@link Reservation} to {@link ReservationView}
 */
@Component
public class ReservationMapper {
    private final StudySpaceService studySpaceService;

    public ReservationMapper(StudySpaceService studySpaceService) {
        this.studySpaceService = studySpaceService;
    }

    public ReservationView convertReservationToReservationView(final Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        StudySpace space = studySpaceService.getStudySpaceById(reservation.getStudySpaceId());

        return new ReservationView(
                reservation.getId(),
                reservation.getReservationId(),
                reservation.getStudentId(),
                reservation.getStudySpaceId(),
                space != null ? space.getName() : reservation.getStudySpaceId(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPresent()
        );
    }
}