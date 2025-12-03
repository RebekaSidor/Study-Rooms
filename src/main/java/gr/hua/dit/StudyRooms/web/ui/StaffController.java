package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.ReservationService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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

        return "staff_home";
    }

    @GetMapping("/staff/reservations")
    public String viewAllReservations(Model model) {

        var reservations = reservationService.getAllReservations();
        model.addAttribute("reservations", reservations);

        return "staff_reservations";
    }

    @GetMapping("/staff/studyspaces")
    public String manageStudySpaces(Model model) {

        var spaces = studySpaceService.getAllStudySpaces();
        model.addAttribute("spaces", spaces);

        return "staff_studyspaces";
    }

    @PostMapping("/staff/reservations/cancel/{id}")
    public String staffCancel(@PathVariable("id") long id) {

        reservationService.deleteReservation(id);
        return "redirect:/staff/reservations?cancelSuccess";
    }

    @GetMapping("/staff/statistics")
    public String showStats(Model model) {

        long totalReservations = reservationService.countAllReservations();
        long activeUsers = reservationService.countActiveUsers();
        var reservationsPerRoom = reservationService.getReservationsPerRoom();
        long fullRoomsToday = reservationService.getFullyBookedRoomsToday();

        model.addAttribute("totalReservations", totalReservations);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("reservationsPerRoom", reservationsPerRoom);
        model.addAttribute("fullRoomsToday", fullRoomsToday);

        return "staff_statistics";
    }

}
