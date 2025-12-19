package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.ReservationService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import gr.hua.dit.StudyRooms.core.service.model.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

        List<HourOption> hours = new ArrayList<>();

        if (studySpaceId != null) {
            StudySpaceView space = allSpaces.stream()
                    .filter(s -> s.studySpaceId().equals(studySpaceId))
                    .findFirst()
                    .orElse(null);

            if (space != null) {
                LocalTime start = space.openingTime();
                LocalTime end = space.closingTime();

                LocalTime now = LocalTime.now(); // τρέχουσα ώρα

                while (!start.isAfter(end.minusHours(1))) {

                    boolean available = !reservationService.existsOverlappingReservation(
                            space.studySpaceId(),
                            LocalDateTime.of(date, start),
                            LocalDateTime.of(date, start.plusHours(1))
                    );

                    boolean pastHour = false;

                    if (date.equals(LocalDate.now()) && start.isBefore(LocalTime.now().plusMinutes(1))) {
                        available = false;
                        pastHour = true;
                    }

                    hours.add(new HourOption(
                            start.format(DateTimeFormatter.ofPattern("HH:mm")),
                            available,
                            pastHour
                    ));

                    start = start.plusHours(1);
                }

            }
        }

        model.addAttribute("hours", hours);
        model.addAttribute("rooms", rooms);
        model.addAttribute("seats", seats);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedSpaceId", studySpaceId);
        model.addAttribute("hours", hours);
        model.addAttribute("todayStr", LocalDate.now().toString());

        return "student_make_reservation";
    }

    @PostMapping("/reserve")
    public String makeReservation(
            @RequestParam String studySpaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String startTime,
            RedirectAttributes redirectAttributes) {

        //no reservations on sundays
        if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Reservations cannot be made on Sundays."
            );
            return "redirect:/student/make-reservation?date=" + date + "&studySpaceId=" + studySpaceId;
        }

        // Έλεγχος για όριο 3 κρατήσεων ανά ημέρα
        String studentId = getCurrentStudentId();
        int reservationsCount = reservationService.getReservationsForStudentOnDate(studentId, date).size();
        if (reservationsCount >= 3) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "You can only make up to 3 reservations for a day."
            );
            return "redirect:/student/make-reservation?date=" + date + "&studySpaceId=" + studySpaceId;
        }


        LocalTime start = LocalTime.parse(startTime);
        LocalDateTime startDateTime = LocalDateTime.of(date, start);
        LocalDateTime endDateTime = startDateTime.plusHours(1);

        boolean studentOverlap =
                reservationService
                        .getReservationsForStudentOnDate(studentId, date)
                        .stream()
                        .anyMatch(r ->
                                r.startTime().isBefore(endDateTime) &&
                                        r.endTime().isAfter(startDateTime)
                        );

        if (studentOverlap) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "You already have another reservation at this time."
            );

            return "redirect:/student/make-reservation?date=" + date +
                    "&studySpaceId=" + studySpaceId;
        }

        // Έλεγχος για υπάρχουσα κράτηση
        if (reservationService.existsOverlappingReservation(studySpaceId, startDateTime, endDateTime)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This time slot is already reserved."
            );
            return "redirect:/student/make-reservation?date=" + date + "&studySpaceId=" + studySpaceId;
        }

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
        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails) {
            return userDetails.getLibraryId();
        }
        throw new IllegalStateException("Principal is not an ApplicationUserDetails");
    }

    @GetMapping("/my-reservations")
    public String showStudentReservations(Authentication auth, Model model) {

        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();

        String libraryId = user.getLibraryId();

        List<ReservationView> reservations =
                reservationService.getReservationsForStudentView(libraryId);

        model.addAttribute("reservations", reservations);

        return "student_reservations";
    }
    @PostMapping("/cancel/{reservationId}")
    public String cancelReservation(@PathVariable("reservationId") Long reservationId,
                                    RedirectAttributes redirectAttributes,
                                    Authentication auth) {

        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        String libraryId = user.getLibraryId();

        boolean cancelled = reservationService.cancelReservation(reservationId, libraryId);

        if (cancelled) {
            redirectAttributes.addFlashAttribute("cancelSuccess", true);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel reservation.");
        }

        return "redirect:/my-reservations";
    }

}
