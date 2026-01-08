package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.service.StudySpaceBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

/**
 * UI controller for managing Study Spaces
 */
@Controller
public class StudySpaceController {

    private final StudySpaceBusinessLogicService studySpaceBusinessLogicService;

    public StudySpaceController(StudySpaceBusinessLogicService studySpaceBusinessLogicService) {
        this.studySpaceBusinessLogicService = studySpaceBusinessLogicService;
    }

/*show available study spaces for guest user*/
    @GetMapping("/showstudyspaces")
    public String showStudySpaces(
            @RequestParam(value = "from", required = false, defaultValue = "mainpage") String from,
            Model model) {

        Map<String, List<StudySpaceView>> data = studySpaceBusinessLogicService.getRoomsAndSeats();
        model.addAllAttributes(data);
        model.addAttribute("from", from);

        return "anonymous_studyspaces";
    }

}