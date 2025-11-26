package gr.hua.dit.StudyRooms.web.ui;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * UI controller for user authentication (login and logout).
 */
@Controller
public class AuthController {
    @GetMapping("/login")
    public String login(){
        //TODO if user is authnticated,redirect to default view
        return "login";
    }

    @GetMapping("/logout")
    public String logout() {
    //TODO if user is not authenticated,redirect to login
        return "logout";
    }
}
