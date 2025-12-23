package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ReservationController {

    private final StudySpaceBusinessLogicService studySpaceBusinessLogicService;
    private final ReservationBusinessLogicService reservationBusinessLogicService;

    public ReservationController(StudySpaceBusinessLogicService studySpaceBusinessLogicService, ReservationBusinessLogicService reservationBusinessLogicService) {
        this.studySpaceBusinessLogicService = studySpaceBusinessLogicService;
        this.reservationBusinessLogicService = reservationBusinessLogicService;
    }

    //show the form for making reservations
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/make-reservation")
    public String showReservationForm(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "studySpaceId", required = false) String studySpaceId,
            Model model) {

        if (date == null) date = LocalDate.now(); //if no date is provided, use today's date

        List<StudySpaceView> allSpaces = studySpaceBusinessLogicService.getAllStudySpaces();
        List<StudySpaceView> rooms = new ArrayList<>();
        List<StudySpaceView> seats = new ArrayList<>();

        //classify each study space by its type
        for (StudySpaceView space : allSpaces) {
            if (space.type() == StudySpaceType.ROOM) rooms.add(space);
            else if (space.type() == StudySpaceType.SEAT) seats.add(space);
        }

        List<HourOption> hours = new ArrayList<>();

        if (studySpaceId != null) {
            //find study space
            StudySpaceView space = allSpaces.stream()
                    .filter(s -> s.studySpaceId().equals(studySpaceId))
                    .findFirst()
                    .orElse(null);

            if (space != null) {
                LocalTime start = space.openingTime();
                LocalTime end = space.closingTime();

                LocalTime now = LocalTime.now();

                //generate 1-hour time slots until closing time
                while (!start.isAfter(end.minusHours(1))) {

                    //check if there is any overlapping reservation
                    boolean available = !reservationBusinessLogicService.existsOverlappingReservation(
                            space.studySpaceId(),
                            LocalDateTime.of(date, start),
                            LocalDateTime.of(date, start.plusHours(1))
                    );

                    //past hours can't be reserved
                    boolean pastHour = false;
                    if (date.equals(LocalDate.now()) && start.isBefore(LocalTime.now().plusMinutes(1))) {
                        available = false;
                        pastHour = true;
                    }
                    //add the hour option to the list
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

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/reserve")
    public String makeReservation(@RequestParam String studySpaceId,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                  @RequestParam String startTime,RedirectAttributes redirectAttributes){

        String studentId = getCurrentStudentId();

        //penalty for 3+ absences
        long absences = reservationBusinessLogicService.getReservationsForStudentOnDate(studentId, LocalDate.now().minusMonths(1))
                .stream()
                .filter(r -> r.endTime().isBefore(LocalDateTime.now()))
                .filter(r -> Boolean.FALSE.equals(r.present()))
                .count();
        if (absences >= 3) {
            redirectAttributes.addFlashAttribute(
                    "penaltyMessage",
                    "⚠ Penalty for not attending reservations – unable to make reservation for 2 days."
            );
            return "redirect:/student/make-reservation?date=" + date + "&studySpaceId=" + studySpaceId;
        }

        //no reservations on Sundays
        if (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Reservations cannot be made on Sundays."
            );
            return "redirect:/student/make-reservation?date=" + date + "&studySpaceId=" + studySpaceId;
        }

        //allow up to 3 reservations per day
        int reservationsCount = reservationBusinessLogicService.getReservationsForStudentOnDate(studentId, date).size();
        if (reservationsCount >= 3) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "You can only make up to 3 reservations for a day."
            );
            return "redirect:/student/make-reservation?date=" + date + "&studySpaceId=" + studySpaceId;
        }


        LocalTime start = LocalTime.parse(startTime);
        LocalDateTime startDateTime = LocalDateTime.of(date, start);
        LocalDateTime endDateTime = startDateTime.plusHours(1);

        //check overlap with student's own reservations
        boolean studentOverlap =
                reservationBusinessLogicService.getReservationsForStudentOnDate(studentId, date)
                                  .stream()
                                  .anyMatch(r -> r.startTime().isBefore(endDateTime) && r.endTime().isAfter(startDateTime));

        if (studentOverlap) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "You already have another reservation at this time."
            );
            return "redirect:/student/make-reservation?date=" + date +
                    "&studySpaceId=" + studySpaceId;
        }

        //check overlap for the study space
        if (reservationBusinessLogicService.existsOverlappingReservation(studySpaceId, startDateTime, endDateTime)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This time slot is already reserved."
            );
            return "redirect:/student/make-reservation?date=" + date + "&studySpaceId=" + studySpaceId;
        }

        //build reservation request object
        CreateReservationRequest request = new CreateReservationRequest(
                null, studentId, studySpaceId, startDateTime, endDateTime
        );

        CreateReservationResult result = reservationBusinessLogicService.createReservation(request);

        if (result.created()) {
            redirectAttributes.addFlashAttribute("successMessage", "Reservation created successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create reservation: " + result.reason());
        }

        return "redirect:/student/make-reservation?date=" + date + "&studySpaceId=" + studySpaceId;
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

        // Καλούμε τη νέα μέθοδο για τον ίδιο μαθητή
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
