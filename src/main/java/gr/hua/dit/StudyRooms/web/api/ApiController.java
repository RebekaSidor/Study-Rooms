package gr.hua.dit.StudyRooms.web.api;

import gr.hua.dit.StudyRooms.core.service.ReservationService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StudySpaceService studySpaceService;
    private final ReservationService reservationService;

    public ApiController(StudySpaceService studySpaceService,
                         ReservationService reservationService) {
        this.studySpaceService = studySpaceService;
        this.reservationService = reservationService;
    }

    // ------------------------------------------
    // GET /api/studyspaces
    // ------------------------------------------
    @GetMapping("/studyspaces")
    public List<StudySpaceView> getSpaces() {
        return studySpaceService.getAllStudySpaces();
    }

    // ------------------------------------------
    // GET /api/reservations  -> staff-only
    // ------------------------------------------
    @GetMapping("/reservations")
    public List<ReservationView> getAllReservations() {
        return reservationService.getAllReservations();
    }

    // ------------------------------------------
    // GET /api/reservations/student/{studentId}
    // ------------------------------------------
    @GetMapping("/reservations/student/{studentId}")
    public List<ReservationView> getStudentReservations(@PathVariable String studentId) {
        return reservationService.getReservationsByStudentId(studentId);
    }

    // ------------------------------------------
    // POST /api/reservations
    // ------------------------------------------
    @PostMapping("/reservations")
    public CreateReservationResult create(@RequestBody CreateReservationRequest req) {
        // ΔΙΟΡΘΩΣΗ: λείπει το 2ο argument!
        return reservationService.createReservation(req, false);
    }
}
