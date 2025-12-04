package gr.hua.dit.StudyRooms.core.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        ApplicationUserDetails user = (ApplicationUserDetails) authentication.getPrincipal();

        if (user.getType().name().equals("LIB_STAFF")) {
            response.sendRedirect("/staff/home");
        } else {
            response.sendRedirect("/profile");
        }
    }
}
