package gr.hua.dit.StudyRooms.web.rest;

import gr.hua.dit.StudyRooms.core.service.StudySpaceDataService;
import gr.hua.dit.StudyRooms.core.service.model.ReservationView;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing {@code StudySpace} resource.
 */
@RestController
@RequestMapping(value = "/api/v1/studyspace", produces = MediaType.APPLICATION_JSON_VALUE)
public class StudySpaceResource {

    private final StudySpaceDataService studySpaceDataService;

    public StudySpaceResource(final StudySpaceDataService studySpaceDataService) {
        if (studySpaceDataService == null) throw new NullPointerException();
        this.studySpaceDataService = studySpaceDataService;
    }

    //Get all study spaces (for guests or staff)
    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping("")
    public List<StudySpaceView> getAllStudySpaces() {
        final List<StudySpaceView> studySpaceViewList = this.studySpaceDataService.getAllStudySpaces();
        return studySpaceViewList;
    }

    //Get availability of a specific study space by date
    @GetMapping("/{id}/availability")
    public List<ReservationView> getStudySpaceAvailability(
            @PathVariable Long id,
            @RequestParam String date
    ) {
        return this.studySpaceDataService.getAvailability(id, date);
    }

}
