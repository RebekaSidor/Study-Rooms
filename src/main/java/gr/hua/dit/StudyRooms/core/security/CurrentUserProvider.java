package gr.hua.dit.StudyRooms.core.security;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Component for providing the current user.
 *
 * @see CurrentUser
 */
@Component
public class CurrentUserProvider {

    public Optional<CurrentUser> getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails) {
            return Optional.of(new CurrentUser(
                    userDetails.personId(),
                    userDetails.getLibraryId(),
                    userDetails.getEmailAddress(),
                    userDetails.getType()
            ));
        }
        return Optional.empty();
    }

    public CurrentUser requireCurrentUser() {
        return this.getCurrentUser().orElseThrow(() -> new SecurityException("not authenticated"));
    }

    public long requiredStudentId() {
        final var currentUser = this.requireCurrentUser();
        if (currentUser.type() != PersonType.STUDENT) throw new SecurityException("Student type/role required");
        return currentUser.personId();
    }
}

