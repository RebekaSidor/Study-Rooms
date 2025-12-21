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
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ReservationService}.
 */
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

/*create a reservation*/
    @Override
    public CreateReservationResult createReservation(CreateReservationRequest request, boolean notify) {
        //find study space
        StudySpace studySpace = studySpaceService.getStudySpaceById(request.studySpaceId());
        if (studySpace == null) {
            return CreateReservationResult.fail("StudySpace not found");
        }

        //check if there is other reservation for the same study space at the same time
        boolean conflict = reservationRepository
                .existsByStudySpaceIdAndEndTimeAfterAndStartTimeBefore(
                        studySpace.getStudySpaceId(),
                        request.startTime(),
                        request.endTime()
                );
        if (conflict) {
            return CreateReservationResult.fail("This timeslot is already reserved.");
        }

        //check if student has other reservation at the same time
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
        //convert to view
        ReservationView view = reservationMapper.convertReservationToReservationView(reservation);

        return CreateReservationResult.success(view);
    }


    //check if there is other reservation for the same study space at the same time
    @Override
    public boolean existsOverlappingReservation(String studySpaceId, LocalDateTime start, LocalDateTime end) {
        //find study-space
        StudySpace studySpace = studySpaceService.getStudySpaceById(studySpaceId);
        if (studySpace == null) {
            return false;
        }
        //check for overlap
        return reservationRepository.existsByStudySpaceIdAndEndTimeAfterAndStartTimeBefore(
                studySpace.getStudySpaceId(),
                start,
                end
        );
    }

    //retrieve all reservations
    @Override
    public List<ReservationView> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::convertReservationToReservationView)
                .toList();
    }

    //count how many reservations there are
    @Override
    public long countAllReservations() {
        return reservationRepository.count();
    }

    //count users that used the application in the last 30 days ~ for statistics page
    @Override
    public long countActiveUsers() {
        LocalDateTime now = LocalDateTime.now().minusDays(30);
        return reservationRepository.countDistinctStudentIdByStartTimeAfter(now);
    }

    //count amount of reservations for each study space
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

    //get all reservations for specific student
    @Override
    public List<ReservationView> getReservationsByStudentId(String studentId) {

        List<Reservation> reservations = reservationRepository.findByStudentId(studentId);

        return reservations.stream()
                .map(reservationMapper::convertReservationToReservationView)
                .toList();
    }

    //calculate number of reservations per hour ~ for statistics page chart
    @Override
    public Map<Integer, Long> getReservationsPerHourForToday() {
        //map working hours to num of reservations
        Map<Integer, Long> reservationsPerHour = new HashMap<>();
        for (int h = 8; h <= 22; h++) {
            reservationsPerHour.put(h, 0L);
        }

        //get reservations from db
        List<Reservation> allReservations = reservationRepository.findAll();

        //calculate
        for (Reservation r : allReservations) {
            int hour = r.getStartTime().getHour();
            if (hour >= 8 && hour <= 22) {
                reservationsPerHour.put(hour,
                        reservationsPerHour.get(hour) + 1);
            }
        }
        return reservationsPerHour;
    }

    //cancel my reservation ~ student
    @Override
    public boolean cancelReservation(Long reservationId, String libraryId) {
        //get the reservation
        var optionalReservation = reservationRepository.findById(reservationId);
        if (optionalReservation.isEmpty()) {
            return false;
        }

        var reservation = optionalReservation.get();

        //check that it's the students reservation - other students can't cancel
        if (!reservation.getStudentId().equals(libraryId)) {
            return false;
        }

        //delete
        reservationRepository.delete(reservation);
        return true;
    }

    //get all student's reservations for specific day
    @Override
    public List<ReservationView> getReservationsForStudentOnDate(String studentId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        //get from db
        List<Reservation> reservations = reservationRepository.findByStudentIdAndStartTimeBetween(
                studentId, startOfDay, endOfDay
        );

        return reservations.stream()
                .map(reservationMapper::convertReservationToReservationView)
                .toList();
    }

}