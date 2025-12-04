package gr.hua.dit.StudyRooms.core.security;

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

    /**
     * Returns the currently authenticated user as a CurrentUser record.
     * If no user is authenticated, returns Optional.empty().
     */
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
}

