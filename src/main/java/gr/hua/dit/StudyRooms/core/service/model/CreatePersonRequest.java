package gr.hua.dit.StudyRooms.core.service.model;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import jakarta.validation.constraints.*;

/**
 * DTO for requesting the creation (registration) of a Person.
 */
public record CreatePersonRequest(
        @NotNull(message = "person type is mandatory")
        PersonType type,

        @Size(max = 20)
        String libraryId,

        @NotBlank(message = "first name is mandatory")
        @Size(max = 100)
        @Pattern(
                regexp = "^[A-Za-zΑ-Ωα-ωΆΈΉΊΌΎΏάέήίόύώ]+$",
                message = "first name must have only letters"
        )
        String firstName,

        @NotBlank(message = "last name is mandatory")
        @Size(max = 100)
        @Pattern(
                regexp = "^[A-Za-zΑ-Ωα-ωΆΈΉΊΌΎΏάέήίόύώ]+$",
                message = "last name must have only letters"
        )
        String lastName,

        @NotBlank(message = "email is mandatory")
        @Size(max = 100)
        @Email(message = "non valid form of email")
        String emailAddress,

        @NotBlank(message = "phone number is mandatory")
        @Size(max = 18)
        String mobilePhoneNumber,

        @NotBlank(message = "Ο κωδικός είναι υποχρεωτικός")
        @Size(min = 4, max = 24, message = "password must have at least 4 characters")
        String rawPassword
) {}