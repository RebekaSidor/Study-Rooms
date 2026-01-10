package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.port.HolidayService;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.ReservationBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.model.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.time.LocalTime;
import java.util.List;

/**
 * UI controller for managing Reservations
 */
@Controller
public class ReservationController {

    private final StudySpaceBusinessLogicService studySpaceBusinessLogicService;
    private final ReservationBusinessLogicService reservationBusinessLogicService;
    private final HolidayService holidayService;

    public ReservationController(StudySpaceBusinessLogicService studySpaceBusinessLogicService, ReservationBusinessLogicService reservationBusinessLogicService, HolidayService holidayService) {
        this.studySpaceBusinessLogicService = studySpaceBusinessLogicService;
        this.reservationBusinessLogicService = reservationBusinessLogicService;
        this.holidayService = holidayService;
    }

    //show the form for making reservations
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/make-reservation")
    public String showReservationForm(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "studySpaceId", required = false) String studySpaceId,
            Model model) {

        if (date == null) date = LocalDate.now();

        // πάρε τα grouped spaces
        var groupedSpaces = studySpaceBusinessLogicService.getAllStudySpacesGrouped();
        model.addAttribute("rooms", groupedSpaces.rooms());
        model.addAttribute("seats", groupedSpaces.seats());

        List<HourOption> hours = (studySpaceId == null)
                ? List.of()
                : reservationBusinessLogicService.getAvailableHours(studySpaceId, date);

        model.addAttribute("hours", hours);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedSpaceId", studySpaceId);
        model.addAttribute("todayStr", LocalDate.now().toString());

        return "student_make_reservation";
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/reserve")
    public String makeReservation(
            @RequestParam String studySpaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String startTime,
            RedirectAttributes redirectAttributes
    ) {
        String studentId = getCurrentStudentId();

        CreateReservationResult result =
                reservationBusinessLogicService.makeReservation(
                        studentId,
                        studySpaceId,
                        date,
                        LocalTime.parse(startTime)
                );

        if (result.created()) {
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Reservation created successfully!"
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", result.reason()
            );
        }

        return "redirect:/student/make-reservation?date=" + date +
                "&studySpaceId=" + studySpaceId;
    }

    //get current users Id
    private String getCurrentStudentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails) {
            return userDetails.getLibraryId();
        }
        throw new IllegalStateException("Principal is not an ApplicationUserDetails");
    }

    //show students reservations
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my-reservations")
    public String showStudentReservations(Authentication auth, Model model) {
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        String libraryId = user.getLibraryId();

        List<ReservationView> reservations = reservationBusinessLogicService.getMyReservations(libraryId);

        model.addAttribute("reservations", reservations);
        return "student_reservations";
    }

    //cancel reservation ~ by student
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/cancel/{reservationId}")
    public String cancelReservation(@PathVariable("reservationId") Long reservationId,
                                    RedirectAttributes redirectAttributes,
                                    Authentication auth) {
        //get current logged-in student
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        String libraryId = user.getLibraryId();

        boolean cancelled = reservationBusinessLogicService.cancelReservation(reservationId, libraryId);
        if (cancelled) {
            redirectAttributes.addFlashAttribute("cancelSuccess", true);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel reservation.");
        }

        return "redirect:/my-reservations";
    }
}