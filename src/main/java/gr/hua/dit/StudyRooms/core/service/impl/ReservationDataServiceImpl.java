package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import gr.hua.dit.StudyRooms.core.service.ReservationDataService;
import gr.hua.dit.StudyRooms.core.service.mapper.ReservationMapper;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Default implementation of {@link ReservationDataService}.
 */
@Service
public class ReservationDataServiceImpl implements ReservationDataService {
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    public ReservationDataServiceImpl(ReservationRepository reservationRepository,
                                      ReservationMapper reservationMapper) {
        if (reservationRepository == null) throw new NullPointerException();
        if (reservationMapper == null) throw new NullPointerException();
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public List<ReservationView> getAllReservations() {
        final List<Reservation> reservationList = this.reservationRepository.findAll();
        final List<ReservationView> reservationViewList = reservationList
                .stream()
                .map(this.reservationMapper::convertReservationToReservationView)
                .toList();
        return reservationViewList;
    }
}
