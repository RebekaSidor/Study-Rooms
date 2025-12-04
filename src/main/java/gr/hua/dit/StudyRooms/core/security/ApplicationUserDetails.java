package gr.hua.dit.StudyRooms.core.security;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;


/**
 * Immutable view implementing Spring's {@link UserDetails} for representing a user in runtime.
 */
public class ApplicationUserDetails implements UserDetails {

    private final long personId;
    private final String libraryId;      // προσθέτουμε
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

    public String getLibraryId() {      // ΥΛΟΠΟΙΗΣΗ GETTER
        return libraryId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public PersonType getType() {
        return type;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return emailAddress; // ή libraryId, ανάλογα τι θες να χρησιμοποιείς ως username
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // Προσθέτεις authorities αν χρειάζεται
    }
}