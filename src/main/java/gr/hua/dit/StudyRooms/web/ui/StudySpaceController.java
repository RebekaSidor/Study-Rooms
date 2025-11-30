package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class StudySpaceController {

    private final StudySpaceService studySpaceService;

    public StudySpaceController(StudySpaceService studySpaceService) {
        this.studySpaceService = studySpaceService;
    }

    @GetMapping("/showstudyspaces")
    public String showStudySpaces(@RequestParam(required = false) String from,
                                  Model model) {
        List<StudySpaceView> all = studySpaceService.getAllStudySpaces();

        List<StudySpaceView> rooms = all.stream()
                .filter(s -> s.type().name().equals("ROOM"))
                .toList();

        List<StudySpaceView> seats = all.stream()
                .filter(s -> s.type().name().equals("SEAT"))
                .toList();

        model.addAttribute("rooms", rooms);
        model.addAttribute("seats", seats);
        model.addAttribute("from", from);

        return "showstudyspaces";
    }

    @GetMapping("/availability")
    public String showAvailabilityPage() {
        return "availability"; // το template με το ημερολόγιο
    }
}