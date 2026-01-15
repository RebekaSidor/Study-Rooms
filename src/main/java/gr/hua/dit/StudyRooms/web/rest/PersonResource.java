package gr.hua.dit.StudyRooms.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.hua.dit.StudyRooms.core.service.PersonDataService;
import gr.hua.dit.StudyRooms.core.service.model.PersonView;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * REST controller for managing {@code Person} resource.
 */
@RestController
@RequestMapping(value = "/api/v1/person", produces = MediaType.APPLICATION_JSON_VALUE)
public class PersonResource {

    private final PersonDataService personDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PersonResource(final PersonDataService personDataService) {
        if (personDataService == null) throw new NullPointerException();
        this.personDataService = personDataService;
    }

    //Get all registered people (for staff/integration)
    @PreAuthorize("hasRole('INTEGRATION_READ')")
    @GetMapping("")
    public List<PersonView> getAllPeople() {
        final List<PersonView> personViewList = this.personDataService.getAllPeople();
        return personViewList;
    }
}
