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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final StudySpaceService studySpaceService;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  ReservationMapper reservationMapper,
                                  StudySpaceService studySpaceService) {

        if (reservationRepository == null) throw new NullPointerException();
        if (reservationMapper == null) throw new NullPointerException();
        if (studySpaceService == null) throw new NullPointerException();

        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.studySpaceService = studySpaceService;
    }

    @Override
    public List<Reservation> getReservationsForStudySpace(String studySpaceId) {

        StudySpace studySpace = studySpaceService.getStudySpaceById(studySpaceId);
        if (studySpace == null) return List.of();

        return reservationRepository.findByStudySpaceId(studySpace.getStudySpaceId());
    }

    @Override
    public CreateReservationResult createReservation(CreateReservationRequest request, boolean notify) {

        //find study space
        StudySpace studySpace = studySpaceService.getStudySpaceById(request.studySpaceId());
        if (studySpace == null) {
            return CreateReservationResult.fail("StudySpace not found");
        }

        //check for overlapp
        boolean conflict = reservationRepository
                .existsByStudySpaceIdAndEndTimeAfterAndStartTimeBefore(
                        studySpace.getStudySpaceId(),
                        request.startTime(),
                        request.endTime()
                );

        if (conflict) {
            return CreateReservationResult.fail("This timeslot is already reserved.");
        }

        // CHECK IF STUDENT HAS ANOTHER RESERVATION OVERLAPPING
        boolean studentHasOverlap =
                reservationRepository.existsByStudentIdAndEndTimeAfterAndStartTimeBefore(
                        request.studentId(),
                        request.startTime(),
                        request.endTime()
                );

        if (studentHasOverlap) {
            throw new IllegalStateException("You already have a reservation at that time.");
        }

        //create Reservation
        Reservation reservation = new Reservation();
        reservation.setReservationId("R" + System.currentTimeMillis());
        reservation.setStudentId(request.studentId());
        reservation.setStudySpaceId(studySpace.getStudySpaceId());
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());

        //save in DB
        reservation = reservationRepository.save(reservation);

        // Mapping to ReservationView for UI
        ReservationView view = reservationMapper.convertReservationToReservationView(reservation);

        return CreateReservationResult.success(view);
    }

    @Override
    public boolean existsOverlappingReservation(String studySpaceId, LocalDateTime start, LocalDateTime end) {

        StudySpace studySpace = studySpaceService.getStudySpaceById(studySpaceId);
        if (studySpace == null) {
            return false;
        }

        return reservationRepository.existsByStudySpaceIdAndEndTimeAfterAndStartTimeBefore(
                studySpace.getStudySpaceId(),
                start,
                end
        );
    }
    @Override
    public List<Reservation> getReservationsForStudent(String studentId) {
        return reservationRepository.findByStudentId(studentId);
    }

    @Override
    public void deleteReservation(long id) {
        reservationRepository.deleteById(id);
    }

    @Override
    public List<Reservation> getUpcomingReservations(String studentId) {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepository.findByStudentId(studentId)
                .stream()
                .filter(r -> r.getStartTime().isAfter(now))
                .toList();
    }

    @Override
    public List<Reservation> getPastReservations(String studentId) {
        LocalDateTime now = LocalDateTime.now();
        return reservationRepository.findByStudentId(studentId)
                .stream()
                .filter(r -> r.getEndTime().isBefore(now))
                .toList();
    }

    @Override
    public List<ReservationView> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::convertReservationToReservationView)
                .toList();
    }

    @Override
    public long countAllReservations() {
        return reservationRepository.count();
    }

    @Override
    public long countActiveUsers() {
        LocalDateTime now = LocalDateTime.now().minusDays(30);
        return reservationRepository.countDistinctStudentIdByStartTimeAfter(now);
    }

    @Override
    public Map<String, Long> getReservationsPerRoom() {

        List<Object[]> results = reservationRepository.countReservationsGroupByStudySpaceId();

        Map<String, Long> map = new HashMap<>();

        for (Object[] row : results) {
            String studySpaceId = (String) row[0];
            Long total = (Long) row[1];
            map.put(studySpaceId, total);
        }
        return map;
    }


    @Override
    public long getFullyBookedRoomsToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.atTime(23,59);

        return reservationRepository.countFullyBookedRooms(from, to);
    }

    @Override
    public List<ReservationView> getReservationsByStudentId(String studentId) {

        List<Reservation> reservations = reservationRepository.findByStudentId(studentId);

        return reservations.stream()
                .map(reservationMapper::convertReservationToReservationView)
                .toList();
    }

    @Override
    public List<ReservationView> getReservationsForStudentView(String studentId) {

        // find users reservations
        var reservations = reservationRepository.findByStudentId(studentId);

        // convert to ReservationView
        return reservations.stream()
                .map(reservationMapper::convertReservationToReservationView)
                .toList();
    }

}
