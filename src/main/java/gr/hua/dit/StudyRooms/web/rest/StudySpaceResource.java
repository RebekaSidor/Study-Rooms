package gr.hua.dit.StudyRooms.web.rest;

import gr.hua.dit.StudyRooms.core.service.StudySpaceDataService;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping("")
    public List<StudySpaceView> studySpaces() {
        final List<StudySpaceView> studySpaceViewList = this.studySpaceDataService.getAllStudySpaces();
        return studySpaceViewList;
    }
}
