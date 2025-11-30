package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import gr.hua.dit.StudyRooms.core.service.ReservationService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import gr.hua.dit.StudyRooms.core.service.mapper.ReservationMapper;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Default implementation of {@link ReservationService}.
 */
@Service
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final StudySpaceService studySpaceService;

    public ReservationServiceImpl(final ReservationRepository reservationRepository,
                                  final ReservationMapper reservationMapper,final StudySpaceService studySpaceService) {
        if (reservationRepository == null) throw new NullPointerException();
        if (reservationMapper == null) throw new NullPointerException();
        if (studySpaceService == null) throw new NullPointerException();

        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.studySpaceService = studySpaceService;
    }

    @Override
    public List<Reservation> getReservationsForStudySpace(String studySpaceId) {
        // Πάρε το StudySpace entity πρώτα
        StudySpace studySpace = studySpaceService.getStudySpaceById(studySpaceId);
        if (studySpace == null) return List.of();

        // Κάλεσε το repository με Long id
        return reservationRepository.findByStudySpace_Id(studySpace.getId());
    }


    @Override
    public CreateReservationResult createReservation(final CreateReservationRequest request, final boolean notify) {
        if (request == null) throw new NullPointerException();

        // Δημιουργία entity
        Reservation reservation = new Reservation();
        reservation.setReservationId(request.reservationId());
        reservation.setStudentId(request.studentId());
        reservation.setTimeslot(request.timeslot().toString());
        reservation.setCreatedAt(Instant.now());

        // Πάρε το StudySpace entity από τη βάση
        StudySpace studySpace = studySpaceService.getStudySpaceById(request.studySpaceId());
        if (studySpace == null) {
            throw new IllegalArgumentException("StudySpace με id " + request.studySpaceId() + " δεν υπάρχει.");
        }
        reservation.setStudySpace(studySpace);
        // Αποθήκευση στη βάση
        reservation = this.reservationRepository.save(reservation);

        // Μετατροπή σε view
        final ReservationView reservationView = this.reservationMapper.convertReservationToReservationView(reservation);

        return CreateReservationResult.success(reservationView);
    }
}
