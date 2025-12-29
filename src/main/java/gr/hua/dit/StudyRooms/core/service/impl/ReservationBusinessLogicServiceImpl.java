package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.Person;
import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.port.HolidayService;
import gr.hua.dit.StudyRooms.core.repository.PersonRepository;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import gr.hua.dit.StudyRooms.core.security.CurrentUser;
import gr.hua.dit.StudyRooms.core.security.CurrentUserProvider;
import gr.hua.dit.StudyRooms.core.service.PersonBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.ReservationBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.mapper.ReservationMapper;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ReservationBusinessLogicService}.
 */
@Service
public class ReservationBusinessLogicServiceImpl implements ReservationBusinessLogicService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationBusinessLogicServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final StudySpaceBusinessLogicService studySpaceBusinessLogicService;
    private final PersonBusinessLogicService personBusinessLogicService;
    private final PersonRepository personRepository;
    private final CurrentUserProvider currentUserProvider;
    private final HolidayService holidayService;

    public ReservationBusinessLogicServiceImpl(ReservationRepository reservationRepository,
                                               ReservationMapper reservationMapper,
                                               StudySpaceBusinessLogicService studySpaceBusinessLogicService,
                                               PersonBusinessLogicService personBusinessLogicService,
                                               PersonRepository personRepository,
                                               CurrentUserProvider currentUserProvider,
                                               HolidayService holidayService) {

        if (reservationRepository == null) throw new NullPointerException();
        if (reservationMapper == null) throw new NullPointerException();
        if (studySpaceBusinessLogicService == null) throw new NullPointerException();
        if (personBusinessLogicService == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();
        if (holidayService == null) throw new NullPointerException();

        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.studySpaceBusinessLogicService = studySpaceBusinessLogicService;
        this.personBusinessLogicService = personBusinessLogicService;
        this.personRepository = personRepository;
        this.currentUserProvider = currentUserProvider;
        this.holidayService = holidayService;
    }

    /*create a reservation*/
    @Transactional
    @Override
    public CreateReservationResult createReservation(CreateReservationRequest request, boolean notify) {
        //check if date is holiday
        LocalDate reservationDate = request.startTime().toLocalDate();
        if (holidayService.isHoliday(reservationDate)) {
            return CreateReservationResult.fail( "Reservations cannot be made on holidays: " + reservationDate );
        }

        //find study space
        StudySpace studySpace = studySpaceBusinessLogicService.getStudySpaceById(request.studySpaceId());
        if (studySpace == null) {
            return CreateReservationResult.fail("StudySpace not found");
        }

        // Find student
        Person student = personBusinessLogicService.getPersonById(request.studentId());
        if (student == null) {
            return CreateReservationResult.fail("Student not found");
        }

        //Security-------------------------------------------
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        if (currentUser.type() != PersonType.STUDENT) {
            throw new SecurityException("Student role required to create reservation");
        }

        if (!currentUser.libraryId().equals(request.studentId())) {
            throw new SecurityException("Authenticated student does not match request studentId");
        }

        //Rules----------------------------------------------
        //check if there is other reservation for the same study space at the same time
        boolean conflict = reservationRepository
                .existsByStudySpaceAndEndTimeAfterAndStartTimeBefore(
                        studySpace,
                        request.startTime(),
                        request.endTime()
                );
        if (conflict) {
            return CreateReservationResult.fail("This timeslot is already reserved.");
        }

        //check if student has other reservation at the same time
        boolean studentHasOverlap =
                reservationRepository.existsByStudentAndEndTimeAfterAndStartTimeBefore(
                        student,
                        request.startTime(),
                        request.endTime()
                );
        if (studentHasOverlap) {
            return CreateReservationResult.fail(
                    "You already have another reservation during this time."
            );
        }

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setReservationId("R" + System.currentTimeMillis());
        reservation.setStudent(student);
        reservation.setStudySpace(studySpace);
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
        StudySpace studySpace = studySpaceBusinessLogicService.getStudySpaceById(studySpaceId);
        if (studySpace == null) {
            return false;
        }
        //check for overlap
        return reservationRepository.existsByStudySpaceAndEndTimeAfterAndStartTimeBefore(
                studySpace,
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
    @Override
    public List<ReservationView> getMyReservations(String studentId) {
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        if (currentUser.type() != PersonType.STUDENT || !currentUser.libraryId().equals(studentId)) {
            throw new SecurityException("Only the student can view their own reservations");
        }

        Person student = personBusinessLogicService.getPersonById(studentId);
        if (student == null) return List.of();

        List<Reservation> reservations = reservationRepository.findByStudent(student);

        return reservations.stream()
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
        //Security-------------------------------------------
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        if (currentUser.type() != PersonType.LIB_STAFF) {
            throw new SecurityException("Staff role required");
        }

        LocalDateTime now = LocalDateTime.now().minusDays(30);
        return reservationRepository.countDistinctStudentsAfter(now);
    }

    //count amount of reservations for each study space
    @Override
    public Map<String, Long> getReservationsPerRoom() {
        //Security-------------------------------------------
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        if (currentUser.type() != PersonType.LIB_STAFF) {
            throw new SecurityException("Staff role required");
        }


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
        //Security----------------------------------------------
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        if (currentUser.type() != PersonType.LIB_STAFF) {
            throw new SecurityException("Only staff can view other students' reservations");
        }

        Person student = personBusinessLogicService.getPersonById(studentId);
        if (student == null) return List.of();

        List<Reservation> reservations = reservationRepository.findByStudent(student);

        return reservations.stream()
                .map(reservationMapper::convertReservationToReservationView)
                .toList();
    }

    //calculate number of reservations per hour ~ for statistics page chart
    @Override
    public Map<Integer, Long> getReservationsPerHourForToday() {

        //Security-------------------------------------
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        if (currentUser.type() != PersonType.LIB_STAFF) {
            throw new SecurityException("Staff role required");
        }

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
    @Transactional
    @Override
    public boolean cancelReservation(Long reservationId, String libraryId) {
        //get the reservation
        var optionalReservation = reservationRepository.findById(reservationId);
        if (optionalReservation.isEmpty()) {
            return false;
        }

        //Security-------------------------------------------

        var reservation = optionalReservation.get();

        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        if (currentUser.type() == PersonType.STUDENT) { //student only cancels his own reservations
            if (!reservation.getStudent().getLibraryId().equals(currentUser.libraryId())) {
                throw new SecurityException("Student cannot cancel another student's reservation");
            }
        }
        else if (currentUser.type() == PersonType.LIB_STAFF) { //staff can cancel any reservation
        }
        else {
            throw new SecurityException("Unsupported role");
        }


        //delete
        reservationRepository.delete(reservation);
        LOGGER.info(
                "Reservation {} cancelled by student {}",
                reservation.getReservationId(),
                libraryId
        );

        return true;
    }

    //get all student's reservations for specific day
    @Override
    public List<ReservationView> getReservationsForStudentOnDate(String studentId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        //Security------------------------------------
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        if (currentUser.type() == PersonType.STUDENT) {
            if (!currentUser.libraryId().equals(studentId)) {
                throw new SecurityException("Student cannot view another student's reservations");
            }
        }
        else if (currentUser.type() == PersonType.LIB_STAFF) {
            // OK
        }
        else {
            throw new SecurityException("Unsupported role");
        }

        //get from db
        Person student = personBusinessLogicService.getPersonById(studentId);
        if (student == null) {
            return List.of();
        }
        List<Reservation> reservations = reservationRepository.findByStudentAndStartTimeBetween(
                student, startOfDay, endOfDay
        );

        return reservations.stream()
                .map(reservationMapper::convertReservationToReservationView)
                .toList();
    }

    @Transactional
    public void clearAbsences(String studentId) {
        Person student = personBusinessLogicService.getPersonById(studentId);
        List<Reservation> reservations = reservationRepository.findByStudent(student);

        for (Reservation r : reservations) {
            if (Boolean.FALSE.equals(r.getPresent())) {
                r.setPresent(null);
                reservationRepository.save(r);
            }
        }
    }

}