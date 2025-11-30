package gr.hua.dit.StudyRooms.web.ui;


import gr.hua.dit.StudyRooms.core.service.ReservationService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;


@Controller
public class AvailabilityController {
    private final StudySpaceService studySpaceService;
    private final ReservationService reservationService;

    public AvailabilityController(StudySpaceService studySpaceService,
                                  ReservationService reservationService) {
        this.studySpaceService = studySpaceService;
        this.reservationService = reservationService;
    }

    @GetMapping("/availability/{id}")
    public String showAvailability(@PathVariable("id") String studySpaceId, Model model) {

        // Πάρε όλες τις ώρες λειτουργίας
        var studySpace = studySpaceService.getStudySpaceById(studySpaceId);

        // Πάρε όλες τις κρατήσεις για αυτό το δωμάτιο/θέση
        var reservations = reservationService.getReservationsForStudySpace(studySpaceId);

        model.addAttribute("studySpace", studySpace);
        model.addAttribute("reservations", reservations);

        return "availability";  // αυτό θα είναι το Thymeleaf template
    }
}
