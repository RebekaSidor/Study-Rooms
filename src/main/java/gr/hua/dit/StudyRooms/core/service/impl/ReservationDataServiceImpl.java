package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.repository.PersonRepository;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import gr.hua.dit.StudyRooms.core.repository.StudySpaceRepository;
import gr.hua.dit.StudyRooms.core.security.CurrentUser;
import gr.hua.dit.StudyRooms.core.security.CurrentUserProvider;
import gr.hua.dit.StudyRooms.core.service.ReservationDataService;
import gr.hua.dit.StudyRooms.core.service.mapper.ReservationMapper;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
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
    private final PersonRepository personRepository;
    private final StudySpaceRepository studySpaceRepository;
    private final CurrentUserProvider currentUserProvider;

    public ReservationDataServiceImpl(ReservationRepository reservationRepository,
                                      ReservationMapper reservationMapper,
                                      PersonRepository personRepository,
                                      StudySpaceRepository studySpaceRepository,
                                      CurrentUserProvider currentUserProvider) {
        if (reservationRepository == null) throw new NullPointerException();
        if (reservationMapper == null) throw new NullPointerException();
        if (personRepository == null) throw new NullPointerException();
        if (studySpaceRepository == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.personRepository = personRepository;
        this.studySpaceRepository = studySpaceRepository;
        this.currentUserProvider = currentUserProvider;
    }

    //Retrieve all reservations (staff/integration)
    public List<ReservationView> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::convertReservationToReservationView)
                .toList();
    }

    //Create a new reservation for a student
    @Override
    public ReservationView createReservation(CreateReservationRequest request) {
        // Find student
        var student = personRepository.findByLibraryId(request.studentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + request.studentId()));

        // Find study space
        var studySpace = studySpaceRepository.findById(Long.valueOf(request.studySpaceId()))
                .orElseThrow(() -> new IllegalArgumentException("Study space not found: " + request.studySpaceId()));

        // Create reservation
        Reservation reservation = new Reservation();
        reservation.setReservationId(request.reservationId() != null ? request.reservationId() : "R" + System.currentTimeMillis());
        reservation.setStudent(student);
        reservation.setStudySpace(studySpace);
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());

        // Save to DB
        Reservation savedReservation = reservationRepository.save(reservation);

        // Convert to view
        return reservationMapper.convertReservationToReservationView(savedReservation);
    }

    //Cancel a reservation by ID (student or staff)
    @Override
    public void cancelReservation(Long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    //Get reservations of the currently authenticated student
    @Override
    public List<ReservationView> getMyReservations() {
        // Get the currently authenticated user
        CurrentUser currentUser = currentUserProvider.requireCurrentUser();

        // Ensure the current user is a student
        if (currentUser.type() != PersonType.STUDENT) {
            throw new SecurityException("Student role required");
        }

        // Get the student's libraryId
        String currentStudentLibraryId = currentUser.libraryId();

        // Find the Person entity
        var student = personRepository.findByLibraryId(currentStudentLibraryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found: " + currentStudentLibraryId
                ));

        // Return all reservations for this student
        return reservationRepository.findByStudent(student)
                .stream()
                .map(reservationMapper::convertReservationToReservationView)
                .toList();
    }
}