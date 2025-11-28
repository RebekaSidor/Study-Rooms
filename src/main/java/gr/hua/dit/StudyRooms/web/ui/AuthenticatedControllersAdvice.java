package gr.hua.dit.StudyRooms.web.ui;

import gr.hua.dit.StudyRooms.core.security.CurrentUser;
import gr.hua.dit.StudyRooms.core.security.CurrentUserProvider;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Provides specific controllers {@link org.springframework.ui.Model} with the current user.
 */
@ControllerAdvice(basePackageClasses = { ProfileController.class })
public class AuthenticatedControllersAdvice {

    private final CurrentUserProvider currentUserProvider;

    public AuthenticatedControllersAdvice(final CurrentUserProvider currentUserProvider) {
        if (currentUserProvider == null) throw new NullPointerException();
        this.currentUserProvider = currentUserProvider;
    }

    @ModelAttribute("me")
    CurrentUser addCurrentUserAsMe() {
        return this.currentUserProvider.getCurrentUser().orElse(null);
    }
}
