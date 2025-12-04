package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.ReservationService;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateReservationResult;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;


    public ReservationController(ReservationService reservationService,
                                 ReservationRepository reservationRepository) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
    }


    @GetMapping("/my-reservations")
    public String showMyReservations(Authentication auth, Model model) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();

        List<Reservation> reservations =
                reservationService.getReservationsForStudent(user.getUsername());

        model.addAttribute("reservations", reservations);

        return "my_reservations";
    }


    @GetMapping("/reserve/{id}")
    public String showReservationForm(@PathVariable("id") String id, Model model) {
        model.addAttribute("studySpaceId", id);
        return "reservation-form";
    }


    @PostMapping("/reserve/{id}")
    public String reserve(
            @RequestParam("date") String dateStr,
            @PathVariable("id") String studySpaceId,
            @RequestParam("start") String start,
            @RequestParam("end") String end,
            Authentication auth,
            Model model
    ) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();

        // only students can make reservation
        if (!user.getAuthorities().toString().contains("STUDENT")) {
            model.addAttribute("error", "Only students can make reservations.");
            return "reservation_error";
        }

        // convert to LocalDateTime
        LocalDateTime startTime = LocalDateTime.parse(dateStr + "T" + start);
        LocalDateTime endTime = LocalDateTime.parse(dateStr + "T" + end);

        //check if study space already has been reserved
        boolean conflict = reservationService.existsOverlappingReservation(studySpaceId, startTime, endTime);
        if (conflict) {
            model.addAttribute("error", "This time range is already reserved.");
            return "reservation_error";
        }

        //check if user has other reservation at this specific time
        boolean userConflict = reservationRepository
                .existsByStudentIdAndEndTimeAfterAndStartTimeBefore(
                        user.getUsername(),
                        startTime,
                        endTime
                );
        if (userConflict) {
            model.addAttribute("error", "You already have another reservation at this time.");
            return "reservation_error";
        }

        CreateReservationRequest request = new CreateReservationRequest(
                "res-" + System.currentTimeMillis(),
                user.getUsername(),
                studySpaceId,
                startTime,
                endTime
        );

        CreateReservationResult result = reservationService.createReservation(request);

        if (!result.created()) {
            model.addAttribute("error", result.reason());
            return "reservation_error";
        }

        model.addAttribute("reservation", result.reservationView());
        return "reservation_success";
    }

    @PostMapping("/cancel/{id}")
    public String cancelReservation(@PathVariable("id") long id, Authentication auth, Model model) {
        if(auth == null || !auth.isAuthenticated()){
            return "redirect:/login";
        }

        String username = ((ApplicationUserDetails) auth.getPrincipal()).getUsername();
        boolean owns = reservationRepository.existsByIdAndStudentId(id, username);

        if (!owns) {
            model.addAttribute("error", "You are not allowed to delete this reservation.");
            return "reservation_error";
        }

        reservationService.deleteReservation(id);
        return "redirect:/my-reservations?cancelSuccess";
    }

    @GetMapping("/my-reservations/upcoming")
    public String showUpcomingReservations(Authentication auth, Model model) {
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        var reservations = reservationService.getUpcomingReservations(user.getUsername());
        model.addAttribute("reservations", reservations);
        return "my_reservations_upcoming";
    }

    @GetMapping("/my-reservations/past")
    public String showPastReservations(Authentication auth, Model model) {
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        var reservations = reservationService.getPastReservations(user.getUsername());
        model.addAttribute("reservations", reservations);
        return "my_reservations_past";
    }
}

