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

import java.time.LocalDateTime;
import java.util.List;

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

        // 1. Βρες το StudySpace
        StudySpace studySpace = studySpaceService.getStudySpaceById(request.studySpaceId());
        if (studySpace == null) {
            return CreateReservationResult.fail("StudySpace not found");
        }

        // 2. Έλεγχος overlapp
        boolean conflict = reservationRepository
                .existsByStudySpaceIdAndEndTimeAfterAndStartTimeBefore(
                        studySpace.getStudySpaceId(),
                        request.startTime(),
                        request.endTime()
                );

        if (conflict) {
            return CreateReservationResult.fail("This timeslot is already reserved.");
        }

        // 3. Δημιουργία Reservation
        Reservation reservation = new Reservation();
        reservation.setReservationId("R" + System.currentTimeMillis());
        reservation.setStudentId(request.studentId());
        reservation.setStudySpaceId(studySpace.getStudySpaceId());
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());

        // 4. Αποθήκευση στη DB
        reservation = reservationRepository.save(reservation);

        // 5. Mapping σε ReservationView για το UI
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

}


//package gr.hua.dit.StudyRooms.core.service.impl;
//
//import gr.hua.dit.StudyRooms.core.model.Reservation;
//import gr.hua.dit.StudyRooms.core.model.StudySpace;
//import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
//import gr.hua.dit.StudyRooms.core.service.ReservationService;
//import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
//import gr.hua.dit.StudyRooms.core.service.mapper.ReservationMapper;
//import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
//import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;
//import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
///**
// * Default implementation of {@link ReservationService}.
// */
//@Service
//public class ReservationServiceImpl implements ReservationService {
//
//    private final ReservationRepository reservationRepository;
//    private final ReservationMapper reservationMapper;
//    private final StudySpaceService studySpaceService;
//
//    public ReservationServiceImpl(final ReservationRepository reservationRepository,
//                                  final ReservationMapper reservationMapper,
//                                  final StudySpaceService studySpaceService) {
//        if (reservationRepository == null) throw new NullPointerException();
//        if (reservationMapper == null) throw new NullPointerException();
//        if (studySpaceService == null) throw new NullPointerException();
//
//        this.reservationRepository = reservationRepository;
//        this.reservationMapper = reservationMapper;
//        this.studySpaceService = studySpaceService;
//    }
//
//    @Override
//    public List<Reservation> getReservationsForStudySpace(String studySpaceId) {
//        // Πάρε το StudySpace entity πρώτα
//        StudySpace studySpace = studySpaceService.getStudySpaceById(studySpaceId);
//        if (studySpace == null) return List.of();
//
//        // Κάλεσε το repository με Long id
//        return reservationRepository.findByStudySpace_Id(studySpace.getId());
//    }
//
//
//    @Override
//    public CreateReservationResult createReservation(CreateReservationRequest request, boolean notify) {
//
//        StudySpace studySpace = studySpaceService.getStudySpaceById(request.studySpaceId());
//        if (studySpace == null) {
//            return CreateReservationResult.fail("StudySpace not found");
//        }
//        // Check overlap
//        boolean conflict = reservationRepository
//                .existsByStudySpace_IdAndEndTimeAfterAndStartTimeBefore(
//                        studySpace.getId(),
//                        request.startTime(),
//                        request.endTime()
//                );
//
//        if (conflict) {
//            return CreateReservationResult.fail("This timeslot is already reserved.");
//        }
//
//        Reservation reservation = new Reservation();
//        reservation.setReservationId("R" + System.currentTimeMillis());
//        reservation.setStudentId(request.studentId());
//        reservation.setStudySpaceId(studySpace.getStudySpaceId());
//
//        reservation.setStartTime(request.startTime());
//        reservation.setEndTime(request.endTime());
//
//        reservation = reservationRepository.save(reservation);
//
//        ReservationView view = reservationMapper.convertReservationToReservationView(reservation);
//
//        return CreateReservationResult.success(view);
//    }
//
//    @Override
//    public boolean existsOverlappingReservation(String studySpaceId, LocalDateTime start, LocalDateTime end) {
//
//        StudySpace studySpace = studySpaceService.getStudySpaceById(studySpaceId);
//        if (studySpace == null) {
//            return false;
//        }
//
//        return reservationRepository.existsByStudySpace_IdAndEndTimeAfterAndStartTimeBefore(
//                studySpace.getId(),
//                start,
//                end
//        );
//    }
//
//
//
//}
