package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.service.PersonBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreatePersonResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

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

        // Log for debugging before calling service
        logger.debug("Received registration request: type={}, firstName={}, lastName={}, email={}, phone={}",
                createPersonRequest.type(),
                createPersonRequest.firstName(),
                createPersonRequest.lastName(),
                createPersonRequest.emailAddress(),
                createPersonRequest.mobilePhoneNumber());

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> logger.warn("Binding error: {}", error));
            return "register";
        }

        CreatePersonResult result = personBusinessLogicService.createPerson(createPersonRequest, true);

        if (result.created()) {
            model.addAttribute("newLibraryId", result.personView().libraryId());
            return "registration_success";
        }

        // Logging failure
        logger.info("Registration failed: {}", result.reason());
        model.addAttribute("errorMessage", result.reason());
        return "register";
    }

}
