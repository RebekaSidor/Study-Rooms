package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import gr.hua.dit.StudyRooms.core.service.PersonService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UI controller for managing profile.
 */
@Controller
public class StudentController {

    private final PersonService personService;
    private final ReservationRepository reservationRepository;

    public StudentController(PersonService personService,
                             ReservationRepository reservationRepository) {
        this.personService = personService;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/profile")
    public String showProfile(Authentication auth, Model model) {
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        model.addAttribute("me", user);

        List<Reservation> reservations = reservationRepository.findByStudentId(user.getLibraryId());

        LocalDateTime now = LocalDateTime.now();
        long absences = reservations.stream()
                // Μόνο κρατήσεις που έχουν τελειώσει
                .filter(r -> r.getEndTime() != null && r.getEndTime().isBefore(now))
                // Και είναι false (απουσία)
                .filter(r -> Boolean.FALSE.equals(r.getPresent()))
                .count();

        model.addAttribute("absences", absences);

        return "student_profile";
    }

    @GetMapping("/profile/change-email")
    public String showEmailForm(Authentication auth, Model model) {
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        model.addAttribute("currentEmail", user.getEmailAddress());
        return "student_change_email";
    }

    @PostMapping("/profile/change-email")
    public String changeEmail(@RequestParam("email") String email,
                              Authentication auth,
                              Model model) {

        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();

        String error = personService.updateEmail(user.getUsername(), email);

        if (error != null) {
            model.addAttribute("error", error);
            return "student_change_email";
        }

        model.addAttribute("success", "Email updated successfully!");
        return "student_change_email";
    }

    @GetMapping("/profile/change-phone")
    public String showChangePhoneForm() {
        return "student_change_phone";
    }


    @PostMapping("/profile/change-phone")
    public String changePhone(@RequestParam("phone") String phone,
                              Authentication auth,
                              Model model) {

        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();

        String error = personService.updatePhone(user.getUsername(), phone);

        if (error != null) {
            model.addAttribute("error", error);
            return "student_change_phone";
        }

        model.addAttribute("success", "Phone number updated successfully!");
        return "student_change_phone";
    }

    @GetMapping("/profile/change-password")
    public String showChangePasswordForm() {
        return "student_change_password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("password") String password,
                                 @RequestParam("confirm") String confirm,
                                 Authentication auth,
                                 Model model) {

        if (!password.equals(confirm)) {
            model.addAttribute("error", "Passwords do not match.");
            return "student_change_password";
        }

        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();

        String error = personService.updatePassword(user.getUsername(), password);

        if (error != null) {
            model.addAttribute("error", error);
            return "student_change_password";
        }

        model.addAttribute("success", "Password updated successfully!");
        return "student_change_password";
    }
}