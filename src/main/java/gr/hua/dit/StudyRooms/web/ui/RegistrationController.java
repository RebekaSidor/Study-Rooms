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
 * Registration UI
 */
@Controller
public class RegistrationController {

    private final PersonService personService;

    public RegistrationController(PersonService personService) {
        if (personService == null) throw new NullPointerException();
        this.personService = personService;
    }


    //html
    @GetMapping("/register")

    public String showRegistrationForm(final Authentication authentication, final Model model){
        if (AuthController.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }
        model.addAttribute("createPersonRequest", new CreatePersonRequest(PersonType.STUDENT, "","", "", "", "", ""));

        return "register";//html template
    }

    //se periptvsh pou kapoios mpei kateu8eian sto registration_succes apo to browser na t bgalei error
    @GetMapping("/registration_success")
    public String registrationSuccess() {
        return "registration_success";
    }


    @PostMapping("/register")
    public String handleFormSubmission(
            final Authentication authentication,
            @ModelAttribute("createPersonRequest") final CreatePersonRequest createPersonRequest,
            final Model model
    ){

        if (AuthController.isAuthenticated(authentication)) {
            return "redirect:/profile"; // already logged in
        }

        final CreatePersonResult createPersonResult = this.personService.createPerson(createPersonRequest, false);

        if (createPersonResult.created()) {
            String newLibraryId = createPersonResult.personView().libraryId();
            model.addAttribute("newLibraryId", newLibraryId);
            return "registration_success"; // <-- SUCCESS PAGE
        }

        model.addAttribute("createPersonRequest", createPersonRequest); //Pass the same form data.
        model.addAttribute("errorMessage", createPersonResult.reason()); //Show an error message!
        return "register";
    }

}
