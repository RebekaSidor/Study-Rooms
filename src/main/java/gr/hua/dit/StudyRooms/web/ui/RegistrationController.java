package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.service.PersonBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonResult;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * UI controller for managing Registration
 */
@Controller
public class RegistrationController {

    private final PersonBusinessLogicService personBusinessLogicService;

    public RegistrationController(PersonBusinessLogicService personBusinessLogicService) {
        if (personBusinessLogicService == null) throw new NullPointerException();
        this.personBusinessLogicService = personBusinessLogicService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(
            final Authentication authentication,
            final Model model
    ) {
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }
        // Initial data for the form.
        final CreatePersonRequest createPersonRequest = new CreatePersonRequest(PersonType.STUDENT, "", "", "", "", "", "");
        model.addAttribute("createPersonRequest", createPersonRequest);
        return "register";
    }

    //in case someone tries accessing directly registration_success from search bar -> show error
    @GetMapping("/registration_success")
    public String registrationSuccess() {
        return "registration_success";
    }

    //handle POST request when form is submited
    @PostMapping("/register")
    public String handleFormSubmission(
            Authentication authentication,
            @Valid @ModelAttribute("createPersonRequest") CreatePersonRequest createPersonRequest,
            BindingResult bindingResult,
            Model model
    ) {
        if (AuthUtils.isAuthenticated(authentication)) {
            return "redirect:/profile";
        }

        // Log για debugging
        System.out.println("Received registration request:");
        System.out.println("Type: " + createPersonRequest.type());
        System.out.println("FirstName: " + createPersonRequest.firstName());
        System.out.println("LastName: " + createPersonRequest.lastName());
        System.out.println("Email: " + createPersonRequest.emailAddress());
        System.out.println("Phone: " + createPersonRequest.mobilePhoneNumber());

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> System.out.println("Binding error: " + error));
            return "register";
        }

        CreatePersonResult result =
                personBusinessLogicService.createPerson(createPersonRequest, true);

        if (result.created()) {
            model.addAttribute("newLibraryId", result.personView().libraryId());
            return "registration_success";
        }

        System.out.println("Registration failed: " + result.reason());
        model.addAttribute("errorMessage", result.reason());
        return "register";
    }

}
