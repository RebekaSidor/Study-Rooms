package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import org.springframework.stereotype.Controller;
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

}
