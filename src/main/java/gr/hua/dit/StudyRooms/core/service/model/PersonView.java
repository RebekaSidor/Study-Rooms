package gr.hua.dit.StudyRooms.core.service.model;

import gr.hua.dit.StudyRooms.core.model.PersonType;

/**
 * PersonView (DTO) that includes only inform to be exposed.
 */
public record PersonView(
        long id,
        String libraryId,
        String firstName,
        String lastName,
        String mobilePhoneNumber,
        String emailAddress,
        PersonType type) {
}
