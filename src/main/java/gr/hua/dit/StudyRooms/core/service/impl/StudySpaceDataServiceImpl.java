package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import gr.hua.dit.StudyRooms.core.repository.StudySpaceRepository;
import gr.hua.dit.StudyRooms.core.service.StudySpaceDataService;
import gr.hua.dit.StudyRooms.core.service.mapper.ReservationMapper;
import gr.hua.dit.StudyRooms.core.service.mapper.StudySpaceMapper;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Default implementation of {@link StudySpaceDataService}.
 */
@Service
public class StudySpaceDataServiceImpl implements StudySpaceDataService {
    private final StudySpaceRepository studySpaceRepository;
    private final StudySpaceMapper studySpaceMapper;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public StudySpaceDataServiceImpl(
            StudySpaceRepository studySpaceRepository,
            StudySpaceMapper studySpaceMapper,
            ReservationRepository reservationRepository,
            ReservationMapper reservationMapper
    ) {
        if (studySpaceRepository == null) throw new NullPointerException();
        if (studySpaceMapper == null) throw new NullPointerException();
        this.studySpaceRepository = studySpaceRepository;
        this.studySpaceMapper = studySpaceMapper;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }

    //Retrieve all study spaces (rooms and seats)
    @Override
    public List<StudySpaceView> getAllStudySpaces() {
        final List<StudySpace> studySpaceList = this.studySpaceRepository.findAll();
        final List<StudySpaceView> studySpaceViewList = studySpaceList
                .stream()
                .map(this.studySpaceMapper::convertStudySpaceToStudySpaceView)
                .toList();
        return studySpaceViewList;
    }

    //Get all reservations for a specific study space on a given date
    @Override
    public List<ReservationView> getAvailability(Long studySpaceId, String date) {

        //convert string → LocalDate
        LocalDate requestedDate = LocalDate.parse(date);

        return reservationRepository.findAll()
                .stream()
                .filter(reservation ->
                        reservation.getStudySpace() != null
                                && reservation.getStudySpace().getId().equals(studySpaceId)
                                && reservation.getStartTime() != null
                                && reservation.getStartTime().toLocalDate().equals(requestedDate)
                )
                .map(reservationMapper::convertReservationToReservationView)
                .toList();
    }
}