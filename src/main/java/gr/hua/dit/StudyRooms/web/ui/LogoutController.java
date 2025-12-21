package gr.hua.dit.StudyRooms.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UI controller for managing logout.
 */
@Controller
public class LogoutController {
    @GetMapping("/logout-page")
    public String showLogoutPage() {
        return "logout";
    }
}
