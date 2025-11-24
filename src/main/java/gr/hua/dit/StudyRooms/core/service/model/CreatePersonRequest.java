package gr.hua.dit.StudyRooms.core.service.model;

import gr.hua.dit.StudyRooms.core.model.PersonType;

/**
 * CreatePersonRequest (DTO).
 */
public record CreatePersonRequest(
        PersonType type,
        String libraryId,
        String firstName,
        String lastName,
        String emailAddress,
        String mobilePhoneNumber,
        String rawPassword
){}
