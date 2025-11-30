package gr.hua.dit.StudyRooms.web.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPageController {
    @GetMapping("/mainpage")
    public String showMainPage() {
        return "mainpage";
    }
}
