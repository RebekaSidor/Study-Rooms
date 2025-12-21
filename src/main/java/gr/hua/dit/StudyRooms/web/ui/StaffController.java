package gr.hua.dit.StudyRooms.web.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.ReservationService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import gr.hua.dit.StudyRooms.core.service.model.NextStudySpaceResponse;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import jakarta.servlet.http.HttpSession;
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

@Controller
public class StaffController {

    private final ReservationService reservationService;
    private final StudySpaceService studySpaceService;
    private final ReservationRepository reservationRepository;

    public StaffController(ReservationService reservationService,
                           StudySpaceService studySpaceService,
                           ReservationRepository reservationRepository) {
        this.reservationService = reservationService;
        this.studySpaceService = studySpaceService;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/staff/home")
    public String staffHome(Authentication auth, Model model) {
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        model.addAttribute("username", user.getUsername());
        return "staff_profile";
    }
/*edit and create new study spaces*/
        @GetMapping("/staff/studyspaces")
        public String manageStudySpaces(Model model) {
            model.addAttribute("spaces", studySpaceService.getAllStudySpaces());
            return "staff_edit_page";
        }

        @GetMapping("/staff/studyspaces/edit/{id}")
        public String editStudySpace(@PathVariable("id") String id, Model model) {
            StudySpace space = studySpaceService.getStudySpaceById(id);
            model.addAttribute("space", space);
            return "staff_edit_studyspace";
        }

    @PostMapping("/staff/studyspaces/edit")
    public String saveStudySpace(@ModelAttribute("space") StudySpace formSpace, Model model) {

        StudySpace existing = studySpaceService.getStudySpaceById(formSpace.getStudySpaceId());
        if (existing == null) {
            throw new IllegalArgumentException("Study space not found");
        }

        // Κρατάμε τα πεδία που δεν αλλάζουν αν είναι null
        if (formSpace.getOpeningTime() != null) {
            existing.setOpeningTime(formSpace.getOpeningTime());
        }

        if (formSpace.getClosingTime() != null) {
            existing.setClosingTime(formSpace.getClosingTime());
        }

        if (existing.getType() == StudySpaceType.ROOM && formSpace.getCapacity() != null) {
            existing.setCapacity(formSpace.getCapacity());
        }

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

        studySpaceService.updateStudySpace(existing);
        return "redirect:/staff/studyspaces?updated";
    }

    @GetMapping("/staff/studyspaces/next")
        @ResponseBody
        public NextStudySpaceResponse getNext(@RequestParam("type") StudySpaceType type) {

            List<StudySpaceView> all = studySpaceService.getAllStudySpaces();

            int max = all.stream()
                    .filter(s -> s.type() == type)
                    .mapToInt(s -> {
                        try {
                            return Integer.parseInt(s.name().substring(1));
                        } catch (Exception e) {
                            return 0;
                        }
                    })
                    .max()
                    .orElse(0);

            int next = max + 1;

            String name = (type == StudySpaceType.ROOM ? "R" : "S") + next;
            String id   = (type == StudySpaceType.ROOM ? "r" : "s") + String.format("%03d", next);

            return new NextStudySpaceResponse(name, id);
        }

        @GetMapping("/staff/studyspaces/create")
        public String createStudySpaceForm(Model model) {
            model.addAttribute("space", new StudySpace());
            return "staff_add_newstudyspace";
        }

        @PostMapping("/staff/studyspaces/create")
        public String saveNewStudySpace(@ModelAttribute("space") StudySpace space, Model model) {

            if (space.getStudySpaceId() == null || space.getName() == null) {
                model.addAttribute("errorMessage", "Invalid study space data");
                return "staff_add_newstudyspace";
            }

            if (space.getClosingTime().isBefore(space.getOpeningTime())) {
                model.addAttribute("errorMessage", "Closing time cannot be before opening time!");
                return "staff_add_newstudyspace";
            }

            if (space.getType() == StudySpaceType.SEAT) {
                space.setCapacity(null);
            }

            studySpaceService.createStudySpace(space);
            return "redirect:/staff/studyspaces?created";
        }

/*show statistics for study spaces*/
        @GetMapping("/staff/statistics")
        public String showStats(Model model) throws Exception {
            model.addAttribute("totalReservations", reservationService.countAllReservations());
            model.addAttribute("activeUsers", reservationService.countActiveUsers());
            model.addAttribute("reservationsPerRoom", reservationService.getReservationsPerRoom());

            Map<Integer, Long> reservationsPerHour = reservationService.getReservationsPerHourForToday();

            List<Integer> hours = new ArrayList<>();
            List<Long> reservations = new ArrayList<>();
            for (int h = 8; h <= 22; h++) {
                hours.add(h);
                reservations.add(reservationsPerHour.getOrDefault(h, 0L));
            }

            ObjectMapper mapper = new ObjectMapper();
            model.addAttribute("hoursJson", mapper.writeValueAsString(hours));
            model.addAttribute("reservationsJson", mapper.writeValueAsString(reservations));

            return "staff_statistics";
        }

/*check attendance of student*/
    @GetMapping("/staff/attendances")
    public String attendances(Model model) {
        List<Reservation> bookings = reservationRepository.findAllByOrderByStartTimeDesc();
        LocalDateTime now = LocalDateTime.now();

        // Ενημέρωση περασμένων κρατήσεων χωρίς τσεκ
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

    @GetMapping("/staff/attendances/toggle/{id}")
    public String toggleAttendance(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow();
        reservation.setPresent(Boolean.TRUE.equals(reservation.getPresent()) ? false : true);
        reservationRepository.save(reservation);
        return "redirect:/staff/attendances";
    }

/*cancel student reservations*/
@GetMapping("/staff/cancel-reservation")
public String showCancelableReservations(Model model, HttpSession session) {

    List<Reservation> futureReservations =
            reservationRepository.findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime.now());

    List<String> history =
            (List<String>) session.getAttribute("history");

    model.addAttribute("futureReservations", futureReservations);
    model.addAttribute("history", history);

    return "staff_cancel";
}


    @PostMapping("/staff/cancel-reservation")
    public String cancelReservation(
            @RequestParam Long selectedReservation,
            @RequestParam String cancelReason,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        Reservation reservation = reservationRepository.findById(selectedReservation).orElse(null);

        if (reservation == null || cancelReason.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Failed to delete reservation. Reason is required."
            );
            return "redirect:/staff/cancel-reservation";
        }

        // History entry
        String historyEntry =
                "Reservation " + reservation.getReservationId() +
                        " (Student: " + reservation.getStudentId() +
                        ", Space: " + reservation.getStudySpaceId() +
                        ", " + reservation.getStartTime() + " – " + reservation.getEndTime() +
                        ") was deleted. Reason: " + cancelReason;

        // Get history from session
        List<String> history =
                (List<String>) session.getAttribute("history");

        if (history == null) {
            history = new ArrayList<>();
        }

        history.add(0, historyEntry); // newest on top
        session.setAttribute("history", history);

        // DELETE from DB
        reservationRepository.delete(reservation);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Reservation deleted successfully."
        );

        return "redirect:/staff/cancel-reservation";
    }


}
