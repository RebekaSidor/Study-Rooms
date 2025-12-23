package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.service.StudySpaceBusinessLogicService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * UI controller for managing guest/anonymous user page.
 */
@Controller
@RequestMapping("/anonymous")
public class AnonymousController {

    private final StudySpaceBusinessLogicService studySpaceBusinessLogicService;

    public AnonymousController(StudySpaceBusinessLogicService studySpaceBusinessLogicService) {
        this.studySpaceBusinessLogicService = studySpaceBusinessLogicService;
    }

    @GetMapping("/menu")
    public String anonymousMenu() {
        return "anonymous_menu";
    }

}
