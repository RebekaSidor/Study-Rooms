package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.service.PersonService;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonResult;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * UI controller for managing Registration
 */
@Controller
public class RegistrationController {

    private final PersonService personService;

    public RegistrationController(PersonService personService) {
        if (personService == null) throw new NullPointerException();
        this.personService = personService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(final Authentication authentication, final Model model){

        if (AuthController.isAuthenticated(authentication)) {
            return "redirect:/profile"; //already logged in
        }
        model.addAttribute("createPersonRequest", new CreatePersonRequest(PersonType.STUDENT, "","", "", "", "", ""));

        return "register";
    }

    //in case someone tries accessing directly registration_success from search bar -> show error
    @GetMapping("/registration_success")
    public String registrationSuccess() {
        return "registration_success";
    }

    //handle POST request when form is submited
    @PostMapping("/register")
    public String handleFormSubmission(final Authentication authentication,
                                       @ModelAttribute("createPersonRequest") final CreatePersonRequest createPersonRequest,
                                       final Model model){

        if (AuthController.isAuthenticated(authentication)) {
            return "redirect:/profile"; //already logged in
        }

        //try creating new Person, return CreatePersonResult
        final CreatePersonResult createPersonResult = this.personService.createPerson(createPersonRequest, false);

        //if success: take new id created and add to Person model
        if (createPersonResult.created()) {
            String newLibraryId = createPersonResult.personView().libraryId();
            model.addAttribute("newLibraryId", newLibraryId);
            return "registration_success"; // <-- SUCCESS PAGE
        }

        //if failed: pass the same form data, error
        model.addAttribute("createPersonRequest", createPersonRequest);
        model.addAttribute("errorMessage", createPersonResult.reason());
        return "register";
    }
}
