package gr.hua.dit.StudyRooms.web.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.ReservationBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.model.NextStudySpaceResponse;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final ReservationRepository reservationRepository;

    public StaffController(ReservationBusinessLogicService reservationBusinessLogicService,
                           StudySpaceBusinessLogicService studySpaceBusinessLogicService,
                           ReservationRepository reservationRepository) {
        this.reservationBusinessLogicService = reservationBusinessLogicService;
        this.studySpaceBusinessLogicService = studySpaceBusinessLogicService;
        this.reservationRepository = reservationRepository;
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
    @PreAuthorize("hasRole('LIB_STAFF')")
    @PostMapping("/staff/studyspaces/edit")
    public String saveStudySpace(@ModelAttribute("space") StudySpace formSpace, Model model) {

        //retrieve from db
        StudySpace existing = studySpaceBusinessLogicService.getStudySpaceById(formSpace.getStudySpaceId());
        if (existing == null) {
            throw new IllegalArgumentException("Study space not found");
        }
        //keep existing fields if there is no change
        if (formSpace.getOpeningTime() != null) {
            existing.setOpeningTime(formSpace.getOpeningTime());
        }
        if (formSpace.getClosingTime() != null) {
            existing.setClosingTime(formSpace.getClosingTime());
        }
        if (existing.getType() == StudySpaceType.ROOM && formSpace.getCapacity() != null) {
            existing.setCapacity(formSpace.getCapacity());
        }

        //define valid time range
        LocalTime earliest = LocalTime.of(8, 0);
        LocalTime latest = LocalTime.of(22, 0);

        if (existing.getOpeningTime() != null && existing.getClosingTime() != null) {

            if (existing.getClosingTime().isBefore(existing.getOpeningTime())) {
                model.addAttribute("space", existing);
                model.addAttribute("errorMessage", "Closing time cannot be before opening time!");
                return "staff_edit_studyspace";
            }

            if (existing.getOpeningTime().isBefore(earliest) ||
                    existing.getClosingTime().isAfter(latest)) {
                model.addAttribute("space", existing);
                model.addAttribute("errorMessage", "Time must be between 08:00 and 22:00!");
                return "staff_edit_studyspace";
            }
        }
        //save
        studySpaceBusinessLogicService.updateStudySpace(existing);
        return "redirect:/staff/studyspaces?updated";
    }

    //return next study space name and id R3->R4
    @GetMapping("/staff/studyspaces/next")
    @ResponseBody
    public NextStudySpaceResponse getNext(@RequestParam("type") StudySpaceType type) {

        List<StudySpaceView> all = studySpaceBusinessLogicService.getAllStudySpaces();

        //find the maximum number currently used for the given type
        int max = all.stream()
              .filter(s -> s.type() == type) //filter by type (ROOM or SEAT)
              .mapToInt(s -> {
              try {
                   return Integer.parseInt(s.name().substring(1)); //extract numeric part of name
              } catch (Exception e) {
                   return 0;
              }
              })
              .max()
              .orElse(0); //if none found, start from 0

        int next = max + 1;
        //generate next name and ID
        String name = (type == StudySpaceType.ROOM ? "R" : "S") + next;
        String id   = (type == StudySpaceType.ROOM ? "r" : "s") + String.format("%03d", next);

        return new NextStudySpaceResponse(name, id);
    }

    //form for creating new study space
    @PreAuthorize("hasRole('LIB_STAFF')")
    @GetMapping("/staff/studyspaces/create")
    public String createStudySpaceForm(Model model) {
        model.addAttribute("space", new StudySpace());
        return "staff_add_newstudyspace";
    }

    //save the new studyspace
    @PreAuthorize("hasRole('LIB_STAFF')")
    @PostMapping("/staff/studyspaces/create")
    public String saveNewStudySpace(@ModelAttribute("space") StudySpace space, Model model) {
        //validate that the study space has ID and name
        if (space.getStudySpaceId() == null || space.getName() == null) {
            model.addAttribute("errorMessage", "Invalid study space data");
            return "staff_add_newstudyspace";
        }
        //validate opening and closing times
        if (space.getClosingTime().isBefore(space.getOpeningTime())) {
            model.addAttribute("errorMessage", "Closing time cannot be before opening time!");
            return "staff_add_newstudyspace";
        }
        //if the space is a SEAT, capacity is not applicable
        if (space.getType() == StudySpaceType.SEAT) {
            space.setCapacity(null);
        }
        //save
        studySpaceBusinessLogicService.createStudySpace(space);
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
    @Transactional
    @PreAuthorize("hasRole('LIB_STAFF')")
    @GetMapping("/staff/attendances")
    public String attendances(Model model) {
        List<Reservation> bookings = reservationRepository.findAllByOrderByStartTimeDesc();
        LocalDateTime now = LocalDateTime.now();

        //for past reservations that staff didn't mark, set present=false
        for (Reservation r : bookings) {
            if (r.getEndTime() != null && r.getEndTime().isBefore(now) && r.getPresent() == null) {
                r.setPresent(false); // Δεν τσέκαρε το staff → false
                reservationRepository.save(r);
            }
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("now", now);

        return "staff_attendance";
    }

    //manually change attendance if student came to his reservation
    @PreAuthorize("hasRole('LIB_STAFF')")
    @GetMapping("/staff/attendances/toggle/{id}")
    public String toggleAttendance(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow();

        //change presence status attended/absent
        reservation.setPresent(Boolean.TRUE.equals(reservation.getPresent()) ? false : true);

        reservationRepository.save(reservation); //save
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
                reservationRepository.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now());

        //get cancellation history from session
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

        //find reservation
        Reservation reservation = reservationRepository.findById(selectedReservation).orElse(null);
        if (reservation == null || cancelReason.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Failed to delete reservation. Reason is required."
            );
            return "redirect:/staff/cancel-reservation";
        }

        //create history entry
        String historyEntry =
                "Reservation " + reservation.getReservationId() +
                        " (Student: " + reservation.getStudent().getLibraryId() +
                        ", Space: " + reservation.getStudySpace().getId() +
                        ", " + reservation.getStartTime() + " – " + reservation.getEndTime() +
                        ") was deleted. Reason: " + cancelReason;


        //get history from session
        List<String> history = (List<String>) session.getAttribute("history");

        if (history == null) {history = new ArrayList<>();}

        history.add(0, historyEntry); // newest on top
        session.setAttribute("history", history);

        //delete from DB
        reservationRepository.delete(reservation);

        redirectAttributes.addFlashAttribute("successMessage", "Reservation deleted successfully.");
        return "redirect:/staff/cancel-reservation";
    }

}
