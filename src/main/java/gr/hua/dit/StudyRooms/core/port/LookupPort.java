package gr.hua.dit.StudyRooms.core.port;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import java.util.Optional;

/**
 * Port to external service for managing lookups
 */
public interface LookupPort {
    Optional<PersonType> lookup(final String libraryId);
}
