package gr.hua.dit.StudyRooms.core.security;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Immutable view implementing Spring's {@link UserDetails} for representing a user in runtime.
 */
public final class ApplicationUserDetails implements UserDetails {

    private final long  personId;
    private final String libraryId;
    private final String passwordHash;
    private final PersonType type;
    private final String emailAddress;

    public ApplicationUserDetails(final long personId,
                                  final String libraryId,
                                  final String passwordHash,
                                  final PersonType type,
                                  final  String emailAddress) {
        if (personId <= 0) throw new IllegalArgumentException();
        if (libraryId == null) throw new NullPointerException();
        if (libraryId .isBlank()) throw new IllegalArgumentException();
        if (passwordHash == null) throw new NullPointerException();
        if (passwordHash.isBlank()) throw new IllegalArgumentException();
        if (type == null) throw new NullPointerException();
        if (emailAddress == null) throw new NullPointerException();
        if (emailAddress.isBlank()) throw new IllegalArgumentException();

        this.personId = personId;
        this.libraryId = libraryId;
        this.passwordHash = passwordHash;
        this.type = type;
        this.emailAddress = emailAddress;
    }

    public long personId() {
        return this.personId;
    }

    public PersonType type() {
        return this.type;
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final String role = (this.type == PersonType.LIB_STAFF) ? "ROLE_LIB_STAFF" : "ROLE_STUDENT";
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.libraryId;
    }

    public String getEmailAddress() {
        return this.emailAddress;
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

    public PersonType getType() {
        return type;
    }
}
