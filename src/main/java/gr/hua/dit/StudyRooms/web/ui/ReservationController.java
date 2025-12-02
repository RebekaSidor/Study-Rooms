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

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
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
}

