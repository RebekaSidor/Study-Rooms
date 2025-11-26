package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.service.PersonService;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonResult;
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
    public String showRegistrationForm(final Model model){

        model.addAttribute("createPersonRequest", new CreatePersonRequest(PersonType.STUDENT, "","", "", "", "", ""));

        return "register";//html template
    }

    @PostMapping("/register")
    public String handleRegistrationFormSubmission(
        @ModelAttribute("createPersonRequest") CreatePersonRequest createPersonRequest,
        final Model model
    ){

        final CreatePersonResult createPersonResult = this.personService.createPerson(createPersonRequest);
        if (createPersonResult.created()) {
            return "/login"; // registration successful - redirect to login form(not yet ready)
        }
        model.addAttribute("createPersonRequest", createPersonRequest); //Pass the same form data.
        model.addAttribute("errorMessage", createPersonResult.reason()); //Show an error message!
        return "register";
    }
}
