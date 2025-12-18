package gr.hua.dit.StudyRooms.web.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.ReservationService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import gr.hua.dit.StudyRooms.core.service.model.NextStudySpaceResponse;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class StaffController {

    private final ReservationService reservationService;
    private final StudySpaceService studySpaceService;

    public StaffController(ReservationService reservationService,
                           StudySpaceService studySpaceService) {
        this.reservationService = reservationService;
        this.studySpaceService = studySpaceService;
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

            if (formSpace.getOpeningTime() != null && formSpace.getClosingTime() != null &&
                    formSpace.getClosingTime().isBefore(formSpace.getOpeningTime())) {
                model.addAttribute("space", formSpace); // null-safe
                model.addAttribute("errorMessage", "Closing time cannot be before opening time!");
                return "staff_edit_studyspace";
            }

            existing.setOpeningTime(formSpace.getOpeningTime());
            existing.setClosingTime(formSpace.getClosingTime());

            if (existing.getType() == StudySpaceType.ROOM) {
                existing.setCapacity(formSpace.getCapacity());
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

            // Δημιουργούμε ώρες λειτουργίας 8-22
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
    public String viewAttendances(Model model) {
        List<ReservationView> bookings = reservationService.getAllReservations();
        model.addAttribute("bookings", bookings);
        return "staff_attendance";
    }


    @GetMapping("/staff/attendances/markPresent/{id}")
        public String markPresent(@PathVariable Long id) {
            reservationService.markAttendance(id, true);
            return "redirect:/staff/attendances";
        }

        @GetMapping("/staff/attendances/markAbsent/{id}")
        public String markAbsent(@PathVariable Long id) {
            reservationService.markAttendance(id, false);
            return "redirect:/staff/attendances";
        }
}
