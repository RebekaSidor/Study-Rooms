package gr.hua.dit.StudyRooms.core.security;

import gr.hua.dit.StudyRooms.core.model.PersonType;

/**
 * @see CurrentUserProvider
 */
public record CurrentUser(long personId, String emailAddress, PersonType type) {
}
