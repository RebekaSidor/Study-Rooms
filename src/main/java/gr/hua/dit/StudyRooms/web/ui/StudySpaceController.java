package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.service.StudySpaceBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

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

        List<StudySpaceView> all = studySpaceBusinessLogicService.getAllStudySpaces();

        //separate to rooms and seats
        List<StudySpaceView> rooms = all.stream()
                .filter(s -> s.type().name().equals("ROOM"))
                .toList();

        List<StudySpaceView> seats = all.stream()
                .filter(s -> s.type().name().equals("SEAT"))
                .toList();

        model.addAttribute("rooms", rooms);
        model.addAttribute("seats", seats);
        model.addAttribute("from", from);

        return "anonymous_studyspaces";
    }

}