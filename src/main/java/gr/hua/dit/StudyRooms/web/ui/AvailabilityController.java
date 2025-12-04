package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.service.ReservationService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
public class AvailabilityController {

    private final StudySpaceService studySpaceService;
    private final ReservationService reservationService;

    public AvailabilityController(StudySpaceService studySpaceService,
                                  ReservationService reservationService) {
        this.studySpaceService = studySpaceService;
        this.reservationService = reservationService;
    }

    // Μικρή helper κλάση για το template
    public static class IntervalRow {
        private final String start;
        private final String end;
        private final boolean reserved;

        public IntervalRow(String start, String end, boolean reserved) {
            this.start = start;
            this.end = end;
            this.reserved = reserved;
        }

        public String getStart() { return start; }
        public String getEnd() { return end; }
        public boolean isReserved() { return reserved; }
    }

    @GetMapping("/availability/{id}")
    public String showAvailability(
            @PathVariable("id") String studySpaceId,
            @RequestParam(value = "date", required = false) String date,
            Model model,
            Authentication authentication
    ) {
        // 1. Ημερομηνία (αν δεν δώσεις, παίρνει σήμερα)
        LocalDate selectedDate = (date == null)
                ? LocalDate.now()
                : LocalDate.parse(date);

        var studySpace = studySpaceService.getStudySpaceById(studySpaceId);

        // 2. Όρια ημέρας (opening/closing)
        LocalTime opening = studySpace.getOpeningTime();
        LocalTime closing = studySpace.getClosingTime();

        LocalDateTime dayStart = selectedDate.atTime(opening);
        LocalDateTime dayEnd = selectedDate.atTime(closing);

        // 3. Κρατήσεις για αυτό το space που "αγγίζουν" τη μέρα αυτή
        List<Reservation> reservations = reservationService.getReservationsForStudySpace(studySpaceId)
                .stream()
                .filter(r -> !r.getEndTime().isBefore(dayStart) &&
                        !r.getStartTime().isAfter(dayEnd))
                .sorted(Comparator.comparing(Reservation::getStartTime))
                .toList();

        // 4. Φτιάχνουμε intervals: Available / Reserved
        List<IntervalRow> intervals = new ArrayList<>();

        LocalDateTime current = dayStart;

        for (Reservation r : reservations) {
            // Κόβουμε τις κρατήσεις στα όρια της ημέρας
            LocalDateTime resStart = r.getStartTime().isBefore(dayStart) ? dayStart : r.getStartTime();
            LocalDateTime resEnd = r.getEndTime().isAfter(dayEnd) ? dayEnd : r.getEndTime();

            // Free interval πριν από την κράτηση
            if (resStart.isAfter(current)) {
                intervals.add(new IntervalRow(
                        current.toLocalTime().toString(),
                        resStart.toLocalTime().toString(),
                        false
                ));
            }

            // Reserved interval
            if (resEnd.isAfter(resStart)) {
                intervals.add(new IntervalRow(
                        resStart.toLocalTime().toString(),
                        resEnd.toLocalTime().toString(),
                        true
                ));
                current = resEnd;
            }
        }

        // Free interval μετά την τελευταία κράτηση
        if (current.isBefore(dayEnd)) {
            intervals.add(new IntervalRow(
                    current.toLocalTime().toString(),
                    dayEnd.toLocalTime().toString(),
                    false
            ));
        }

        // 5. Δίνουμε δεδομένα στο template
        model.addAttribute("studySpaceId", studySpaceId);
        model.addAttribute("studySpace", studySpace);
        model.addAttribute("date", selectedDate);
        model.addAttribute("intervals", intervals);

        return "availability-simple";
    }
}
