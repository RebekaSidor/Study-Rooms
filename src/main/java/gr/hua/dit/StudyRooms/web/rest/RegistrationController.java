package gr.hua.dit.StudyRooms.web.rest;

import gr.hua.dit.StudyRooms.core.model.Person;
import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.repository.PersonRepository;
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

    private final PersonRepository personRepository;

    public RegistrationController(PersonRepository personRepository) {
        if (personRepository == null) throw new NullPointerException();
        this.personRepository = personRepository;
    }


    //html
    @GetMapping("/register")
    public String showRegistrationForm(final Model model){

        model.addAttribute("person", new Person(null, "","", "", "", "", PersonType.STUDENT , ""));

        return "register";//html template
    }

    @PostMapping("/register")
    public String handleRegistrationFormSubmission(
        @ModelAttribute("person") Person person,
        final Model model
    ){
        System.out.println(person.toString());
        person = this.personRepository.save(person);
        System.out.println(person.toString());
        model.addAttribute("person", person);

        return "redirect:/login";//registration successful
    }
}
