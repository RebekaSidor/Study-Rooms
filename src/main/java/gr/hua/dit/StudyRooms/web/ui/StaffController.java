package gr.hua.dit.StudyRooms.web.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.ReservationBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.model.NextStudySpaceResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ValidationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UI controller for managing Library Staff Profil
 */
@Controller
public class StaffController {

    private final ReservationBusinessLogicService reservationBusinessLogicService;
    private final StudySpaceBusinessLogicService studySpaceBusinessLogicService;

    public StaffController(ReservationBusinessLogicService reservationBusinessLogicService,
                           StudySpaceBusinessLogicService studySpaceBusinessLogicService) {
        this.reservationBusinessLogicService = reservationBusinessLogicService;
        this.studySpaceBusinessLogicService = studySpaceBusinessLogicService;
    }

    //show library staff profil
    @PreAuthorize("hasRole('LIB_STAFF')")
    @GetMapping("/staff/home")
    public String staffHome(Authentication auth, Model model) {
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        model.addAttribute("username", user.getUsername());
        return "staff_profile";
    }

/**
* edit and create new study spaces
*/
    //general edit page for study spaces
    @PreAuthorize("hasRole('LIB_STAFF')")
    @GetMapping("/staff/studyspaces")
    public String manageStudySpaces(Model model) {
        model.addAttribute("spaces", studySpaceBusinessLogicService.getAllStudySpaces());
            return "staff_edit_page";
    }

    //edit page for chosen study space
    @PreAuthorize("hasRole('LIB_STAFF')")
    @GetMapping("/staff/studyspaces/edit/{id}")
    public String editStudySpace(@PathVariable("id") String id, Model model) {
         StudySpace space = studySpaceBusinessLogicService.getStudySpaceById(id);
         model.addAttribute("space", space);
         return "staff_edit_studyspace";
    }

    //save changes made to study space
    @PostMapping("/staff/studyspaces/edit")
    @PreAuthorize("hasRole('LIB_STAFF')")
    public String saveStudySpace(@ModelAttribute("space") StudySpace formSpace, Model model) {
        StudySpace existing = studySpaceBusinessLogicService.getStudySpaceById(formSpace.getStudySpaceId());
        if (existing == null) {
            throw new IllegalArgumentException("Study space not found");
        }

        try {
            studySpaceBusinessLogicService.validateAndUpdateStudySpace(existing, formSpace);
        } catch (ValidationException e) {
            model.addAttribute("space", existing);
            model.addAttribute("errorMessage", e.getMessage());
            return "staff_edit_studyspace";
        }

        return "redirect:/staff/studyspaces?updated";
    }

    //return next study space name and id R3->R4
    @GetMapping("/staff/studyspaces/next")
    @ResponseBody
    public NextStudySpaceResponse getNext(@RequestParam("type") StudySpaceType type) {
        return studySpaceBusinessLogicService.getNextStudySpace(type);
    }

    //form for creating new study space
    @PreAuthorize("hasRole('LIB_STAFF')")
    @GetMapping("/staff/studyspaces/create")
    public String createStudySpaceForm(Model model) {
        model.addAttribute("space", new StudySpace());
        return "staff_add_newstudyspace";
    }

    //save the new studyspace
    @PostMapping("/staff/studyspaces/create")
    @PreAuthorize("hasRole('LIB_STAFF')")
    public String saveNewStudySpace(@ModelAttribute("space") StudySpace space, Model model) {
        try {
            studySpaceBusinessLogicService.validateAndCreateStudySpace(space);
        } catch (ValidationException e) {
            model.addAttribute("space", space);
            model.addAttribute("errorMessage", e.getMessage());
            return "staff_add_newstudyspace";
        }
        return "redirect:/staff/studyspaces?created";
    }

    /**
 * show statistics for study spaces
 * */
    @PreAuthorize("hasRole('LIB_STAFF')")
    @GetMapping("/staff/statistics")
    public String showStats(Model model) throws Exception {
         model.addAttribute("totalReservations", reservationBusinessLogicService.countAllReservations());
         model.addAttribute("activeUsers", reservationBusinessLogicService.countActiveUsers());
         model.addAttribute("reservationsPerRoom", reservationBusinessLogicService.getReservationsPerRoom());

         //get reservations per hour for today
         Map<Integer, Long> reservationsPerHour = reservationBusinessLogicService.getReservationsPerHourForToday();

         //prepare lists for char
         List<Integer> hours = new ArrayList<>();
         List<Long> reservations = new ArrayList<>();
         for (int h = 8; h <= 22; h++) {
            hours.add(h);
            reservations.add(reservationsPerHour.getOrDefault(h, 0L));
         }

         //convert lists to JSON for frontend
         ObjectMapper mapper = new ObjectMapper();
         model.addAttribute("hoursJson", mapper.writeValueAsString(hours));
         model.addAttribute("reservationsJson", mapper.writeValueAsString(reservations));

         return "staff_statistics";
    }

/**
 * check attendance of student
 * */
    //show student reservation and attendances
    @PreAuthorize("hasRole('LIB_STAFF')")
    @GetMapping("/staff/attendances")
    public String attendances(Model model) {

        List<Reservation> bookings =
                reservationBusinessLogicService.getReservationsForAttendanceAndAutoMarkAbsents();

        model.addAttribute("bookings", bookings);
        model.addAttribute("now", reservationBusinessLogicService.getCurrentTime());

        return "staff_attendance";
    }


    //manually change attendance if student came to his reservation
    @PreAuthorize("hasRole('LIB_STAFF')")
    @GetMapping("/staff/attendances/toggle/{id}")
    public String toggleAttendance(@PathVariable Long id) {
        reservationBusinessLogicService.toggleAttendance(id);
        return "redirect:/staff/attendances";
    }

    /**
 * cancel student reservations
 * */
    //show future student reservations and cancel form
    @PreAuthorize("hasRole('LIB_STAFF')")
    @GetMapping("/staff/cancel-reservation")
    public String showCancelableReservations(Model model, HttpSession session) {

        List<Reservation> futureReservations =
                reservationBusinessLogicService.getFutureReservations();

        List<String> history = (List<String>) session.getAttribute("history");

        model.addAttribute("futureReservations", futureReservations);
        model.addAttribute("history", history);

        return "staff_cancel";
    }

    //make cancellation ~ by library staff
    //history only visible for session, if browser closed history lost
    @PreAuthorize("hasRole('LIB_STAFF')")
    @PostMapping("/staff/cancel-reservation")
    public String cancelReservation(
            @RequestParam Long selectedReservation,
            @RequestParam String cancelReason,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (cancelReason == null || cancelReason.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Cancellation reason is required."
            );
            return "redirect:/staff/cancel-reservation";
        }

        boolean success = reservationBusinessLogicService
                .cancelReservationByStaff(selectedReservation, cancelReason);

        if (!success) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Reservation not found or could not be cancelled."
            );
            return "redirect:/staff/cancel-reservation";
        }

        //history
        List<String> history = (List<String>) session.getAttribute("history");
        if (history == null) {
            history = new ArrayList<>();
        }

        history.add(0,
                "Reservation ID " + selectedReservation +
                        " was cancelled. Reason: " + cancelReason
        );

        session.setAttribute("history", history);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Reservation cancelled successfully."
        );

        return "redirect:/staff/cancel-reservation";
    }
    @PostMapping("/staff/apply-penalty")
    @PreAuthorize("hasRole('LIB_STAFF')")
    public String applyPenalty(@RequestParam String studentId, RedirectAttributes redirectAttributes) {
        reservationBusinessLogicService.applyPenalty(studentId);
        redirectAttributes.addFlashAttribute("successMessage", "Penalty sent to student " + studentId);
        return "redirect:/staff/attendances";
    }
}