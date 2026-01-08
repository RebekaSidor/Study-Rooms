package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.port.LibraryDirections;
import gr.hua.dit.StudyRooms.core.port.impl.LibraryDirectionsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UI controller for managing homepage.
 */
@Controller
public class HomepageController {

    private final LibraryDirections libraryDirections;

    public HomepageController(LibraryDirections libraryDirections) {
        this.libraryDirections = libraryDirections;
    }

    @GetMapping("/")
    public String showHomepage(Authentication authentication, Model model) {
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }

        LibraryDirectionsImpl.MapResponse response = libraryDirections.getDirections();
        model.addAttribute("directionsUrl", response.getDirectionsUrl());
        model.addAttribute("destination", response.getDestination());

        return "homepage";
    }

}