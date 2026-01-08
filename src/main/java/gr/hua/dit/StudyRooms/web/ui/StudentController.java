package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.Person;
import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.repository.PersonRepository;
import gr.hua.dit.StudyRooms.core.repository.ReservationRepository;
import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import gr.hua.dit.StudyRooms.core.service.ReservationBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.model.StudentStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import gr.hua.dit.StudyRooms.core.service.PersonBusinessLogicService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * UI controller for managing Student Profile.
 */
@Controller
public class StudentController {

    private final PersonBusinessLogicService personBusinessLogicService;
    private final ReservationBusinessLogicService reservationBusinessLogicService;
    private final ReservationRepository reservationRepository;
    private final PersonRepository personRepository;

    public StudentController(PersonBusinessLogicService personBusinessLogicService,
                             ReservationRepository reservationRepository,
                             PersonRepository personRepository,
                             ReservationBusinessLogicService reservationBusinessLogicService) {
        this.personBusinessLogicService = personBusinessLogicService;
        this.reservationRepository = reservationRepository;
        this.personRepository = personRepository;
        this.reservationBusinessLogicService = reservationBusinessLogicService;
    }

    //show student profil
    @GetMapping("/profile")
    public String showProfile(Authentication auth, Model model) {
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        model.addAttribute("me", user);

        Person student = personBusinessLogicService.getPersonById(user.getLibraryId());

        if (student == null) {
            model.addAttribute("absences", 0L);
            model.addAttribute("hasPenalty", false);
            model.addAttribute("penaltyUntil", null);
            return "student_profile";
        }

        StudentStatus status = personBusinessLogicService.calculateStudentStatus(student);

        model.addAttribute("absences", status.absences());
        model.addAttribute("hasPenalty", status.hasPenalty());
        model.addAttribute("penaltyUntil", status.penaltyUntil());

        return "student_profile";
    }

    /**
     * change personal info
     * */
    //show form for changing email
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/profile/change-email")
    public String showEmailForm(Authentication auth, Model model) {
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        model.addAttribute("currentEmail", user.getEmailAddress());
        return "student_change_email";
    }
    //make change
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/profile/change-email")
    public String changeEmail(@RequestParam("email") String email, Authentication auth, Model model) {
        //get logged-in user
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        //call service
        String error = personBusinessLogicService.updateEmail(user.getUsername(), email);

        if (error != null) {
            model.addAttribute("error", error); //fail
            return "student_change_email";
        }

        model.addAttribute("success", "Email updated successfully!"); //success
        return "student_change_email";
    }

    //show form for changing phone
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/profile/change-phone")
    public String showChangePhoneForm() {return "student_change_phone";}
    //make change
    @PostMapping("/profile/change-phone")
    public String changePhone(@RequestParam("phone") String phone, Authentication auth, Model model) {
        //get logged-in user
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        //call service
        String error = personBusinessLogicService.updatePhone(user.getUsername(), phone);

        if (error != null) {
            model.addAttribute("error", error); //fail
            return "student_change_phone";
        }

        model.addAttribute("success", "Phone number updated successfully!"); //success
        return "student_change_phone";
    }

    //show form for changing password
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/profile/change-password")
    public String showChangePasswordForm() {return "student_change_password";}
    //make change
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("password") String password, @RequestParam("confirm") String confirm, Authentication auth, Model model) {

        if (!password.equals(confirm)) {
            model.addAttribute("error", "Passwords do not match.");
            return "student_change_password";
        }
        //get logged-in user
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        //call service
        String error = personBusinessLogicService.updatePassword(user.getUsername(), password);

        if (error != null) {
            model.addAttribute("error", error); //fail
            return "student_change_password";
        }

        model.addAttribute("success", "Password updated successfully!"); //success
        return "student_change_password";
    }
}