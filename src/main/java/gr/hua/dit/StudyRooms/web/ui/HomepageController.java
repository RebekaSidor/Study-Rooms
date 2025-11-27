package gr.hua.dit.StudyRooms.web.ui;


import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UI controller for managing homepage.
 */
@Controller
public class HomepageController {

    @GetMapping("/")
    public String showHomepage(final Authentication authentication) {
        if (AuthController.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }
        return "homepage";
    }
}