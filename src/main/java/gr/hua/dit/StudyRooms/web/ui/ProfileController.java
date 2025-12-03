package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.security.ApplicationUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import gr.hua.dit.StudyRooms.core.service.PersonService;


/**
 * UI controller for managing profile.
 */
@Controller
public class ProfileController {

    private final PersonService personService;

    public ProfileController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping("/profile")
    public String showProfile() {
        return "profile";
    }

    @GetMapping("/profile/change-email")
    public String showEmailForm(Authentication auth, Model model) {
        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();
        model.addAttribute("currentEmail", user.getEmailAddress());
        return "change_email";
    }

    @PostMapping("/profile/change-email")
    public String changeEmail(@RequestParam("email") String email,
                              Authentication auth,
                              Model model) {

        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();

        String error = personService.updateEmail(user.getUsername(), email);

        if (error != null) {
            model.addAttribute("error", error);
            return "change_email";
        }

        model.addAttribute("success", "Email updated successfully!");
        return "change_email";
    }

    @GetMapping("/profile/change-phone")
    public String showChangePhoneForm() {
        return "change_phone";
    }


    @PostMapping("/profile/change-phone")
    public String changePhone(@RequestParam("phone") String phone,
                              Authentication auth,
                              Model model) {

        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();

        String error = personService.updatePhone(user.getUsername(), phone);

        if (error != null) {
            model.addAttribute("error", error);
            return "change_phone";
        }

        model.addAttribute("success", "Phone number updated successfully!");
        return "change_phone";
    }

    @GetMapping("/profile/change-password")
    public String showChangePasswordForm() {
        return "change_password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("password") String password,
                                 @RequestParam("confirm") String confirm,
                                 Authentication auth,
                                 Model model) {

        if (!password.equals(confirm)) {
            model.addAttribute("error", "Passwords do not match.");
            return "change_password";
        }

        ApplicationUserDetails user = (ApplicationUserDetails) auth.getPrincipal();

        String error = personService.updatePassword(user.getUsername(), password);

        if (error != null) {
            model.addAttribute("error", error);
            return "change_password";
        }

        model.addAttribute("success", "Password updated successfully!");
        return "change_password";
    }

}