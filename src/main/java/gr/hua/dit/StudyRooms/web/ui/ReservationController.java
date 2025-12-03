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


import java.time.LocalDate;
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

        return "my_reservations"; // θα το φτιάξουμε τώρα
    }


    @GetMapping("/reserve/{id}")
    public String showReservationForm(@PathVariable("id") String id, Model model) {

        model.addAttribute("studySpaceId", id);

        return "reservation-form"; // η φόρμα που θα ανοίγει
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
        // Πρέπει να είναι logged in
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // Πάρε τον χρήστη (student)
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();

        // **ΜΟΝΟ STUDENTS ΜΠΟΡΟΥΝ ΝΑ ΚΑΝΟΥΝ RESERVATION**
        if (!user.getAuthorities().toString().contains("STUDENT")) {
            model.addAttribute("error", "Only students can make reservations.");
            return "reservation_error";
        }

        // Μετατροπή σε LocalDateTime
        LocalDateTime startTime = LocalDateTime.parse(dateStr + "T" + start);
        LocalDateTime endTime = LocalDateTime.parse(dateStr + "T" + end);


        // CHECK FOR OVERLAP
        boolean conflict = reservationService.existsOverlappingReservation(studySpaceId, startTime, endTime);

        if (conflict) {
            model.addAttribute("error", "This time range is already reserved.");
            return "reservation_error";
        }

        // ⭐ NEW: CHECK IF USER HAS ANOTHER RESERVATION AT THE SAME TIME ⭐

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

        // Δημιουργία request
        CreateReservationRequest request = new CreateReservationRequest(
                "res-" + System.currentTimeMillis(),   // reservationId
                user.getUsername(),   // studentId = libraryId
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
        // prepei na einai logged in
        if(auth == null || !auth.isAuthenticated()){
            return "redirect:/login";
        }

        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();

        // fere oles tis krathseis toy user
        List<Reservation> myReservations = reservationService.getReservationsForStudent(user.getUsername());

        // elegxos - an h krathsh den anhkei se auton apagoreyetai
        boolean owns = myReservations.stream().anyMatch(reservation -> reservation.getId() == id);
        if(!owns) {
            model.addAttribute("error", "You are not allowed to delete this reservation.");
            return "reservation_error";
        }

        // DELETE
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

