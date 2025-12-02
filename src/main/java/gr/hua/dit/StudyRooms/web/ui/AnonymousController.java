package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/anonymous")
public class AnonymousController {

    private final StudySpaceService studySpaceService;

    public AnonymousController(StudySpaceService studySpaceService) {
        this.studySpaceService = studySpaceService;
    }

    @GetMapping("/menu")
    public String anonymousMenu() {
        return "anonymous_menu";
    }

    // ----- 1) Rooms -----
    @GetMapping("/rooms")
    public String viewRooms(Model model) {
        model.addAttribute("rooms",
                studySpaceService.getAllStudySpaces()
                        .stream()
                        .filter(space -> space.type() == StudySpaceType.ROOM)
                        .toList()
        );
        return "anonymous_rooms";
    }

    // ----- 2) Seats -----
    @GetMapping("/seats")
    public String viewSeats(Model model) {
        model.addAttribute("seats",
                studySpaceService.getAllStudySpaces()
                        .stream()
                        .filter(space -> space.type() == StudySpaceType.SEAT)
                        .toList()
        );
        return "anonymous_seats";
    }

}
