package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import gr.hua.dit.StudyRooms.core.service.ReservationService;
import gr.hua.dit.StudyRooms.core.service.mapper.ReservationMapper;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Default implementation of {@link ReservationService}.
 */
@Service
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public ReservationServiceImpl(final ReservationRepository reservationRepository,
                                  final ReservationMapper reservationMapper) {
        if (reservationRepository == null) throw new NullPointerException();
        if (reservationMapper == null) throw new NullPointerException();

        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public CreateReservationResult createReservation(final CreateReservationRequest request, final boolean notify) {
        if (request == null) throw new NullPointerException();

        // Δημιουργία entity
        Reservation reservation = new Reservation();
        reservation.setReservationId(request.reservationId());
        reservation.setStudentId(request.studentId());
        reservation.setStudySpaceId(request.studySpaceId());
        reservation.setCreatedAt(Instant.now());

        // Αποθήκευση στη βάση
        reservation = this.reservationRepository.save(reservation);

        // Μετατροπή σε view
        final ReservationView reservationView = this.reservationMapper.convertReservationToReservationView(reservation);

        return CreateReservationResult.success(reservationView);
    }
}
