package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.ReservationService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ReservationController {

    private final StudySpaceService studySpaceService;
    private final ReservationService reservationService;

    public ReservationController(StudySpaceService studySpaceService, ReservationService reservationService) {
        this.studySpaceService = studySpaceService;
        this.reservationService = reservationService;
    }

    @GetMapping("/student/make-reservation")
    public String showReservationForm(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "studySpaceId", required = false) String studySpaceId,
            Model model) {

        if (date == null) date = LocalDate.now();

        List<StudySpaceView> allSpaces = studySpaceService.getAllStudySpaces();
        List<StudySpaceView> rooms = new ArrayList<>();
        List<StudySpaceView> seats = new ArrayList<>();

        for (StudySpaceView space : allSpaces) {
            if (space.type() == StudySpaceType.ROOM) rooms.add(space);
            else if (space.type() == StudySpaceType.SEAT) seats.add(space);
        }

        List<String> availableHours = new ArrayList<>();
        if (studySpaceId != null) {
            StudySpaceView space = allSpaces.stream()
                    .filter(s -> s.studySpaceId().equals(studySpaceId))
                    .findFirst()
                    .orElse(null);
            if (space != null) {
                LocalTime start = space.openingTime();
                LocalTime end = space.closingTime();
                while (!start.isAfter(end.minusHours(1))) {
                    if (!reservationService.existsOverlappingReservation(
                            space.studySpaceId(),
                            LocalDateTime.of(date, start),
                            LocalDateTime.of(date, start.plusHours(1))
                    )) {
                        availableHours.add(start.format(DateTimeFormatter.ofPattern("HH:mm")));
                    }
                    start = start.plusHours(1);
                }
            }
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("seats", seats);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedSpaceId", studySpaceId);
        model.addAttribute("availableHours", availableHours);

        return "student_make_reservation";
    }

    @PostMapping("/reserve")
    public String makeReservation(
            @RequestParam String studySpaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String startTime,
            RedirectAttributes redirectAttributes) {

        LocalTime start = LocalTime.parse(startTime);
        LocalDateTime startDateTime = LocalDateTime.of(date, start);
        LocalDateTime endDateTime = startDateTime.plusHours(1);

        String studentId = getCurrentStudentId();

        CreateReservationRequest request = new CreateReservationRequest(
                null, studentId, studySpaceId, startDateTime, endDateTime
        );

        CreateReservationResult result = reservationService.createReservation(request);

        if (result.created()) {
            redirectAttributes.addFlashAttribute("successMessage", "Reservation created successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create reservation: " + result.reason());
        }

        return "redirect:/student/make-reservation?date=" + date + "&studySpaceId=" + studySpaceId;
    }

    private String getCurrentStudentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails) {
            return userDetails.getLibraryId(); // επιστρέφει το libraryId (π.χ. lib2025000)
        }

        throw new IllegalStateException("Principal is not an ApplicationUserDetails");
    }

    @GetMapping("/my-reservations")
    public String showStudentReservations(Authentication auth, Model model) {
        String studentId = auth.getName();

        List<ReservationView> reservations = reservationService.getReservationsForStudentView(studentId);

        model.addAttribute("reservations", reservations);

        return "student_reservations";
    }

}
