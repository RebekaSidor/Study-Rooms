package gr.hua.dit.StudyRooms.core.security;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Immutable view implementing Spring's {@link UserDetails} for representing a user in runtime.
 */
public class ApplicationUserDetails implements UserDetails {

    private final long personId;
    private final String libraryId;
    private final String emailAddress;
    private final PersonType type;
    private final String password;

    public ApplicationUserDetails(long personId,
                                  String libraryId,
                                  String emailAddress,
                                  PersonType type,
                                  String password) {
        this.personId = personId;
        this.libraryId = libraryId;
        this.emailAddress = emailAddress;
        this.type = type;
        this.password = password;
    }

    public long personId() {
        return personId;
    }

    public String getLibraryId() {
        return libraryId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public PersonType getType() {
        return type;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final String role;
        if (this.type == PersonType.STUDENT) {
            role = "ROLE_STUDENT";
        } else if (this.type == PersonType.LIB_STAFF) {
            role = "ROLE_LIB_STAFF";
        } else {
            throw new RuntimeException("Invalid type: " + this.type);
        }
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return emailAddress;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}