package gr.hua.dit.StudyRooms.core.security;

import gr.hua.dit.StudyRooms.core.repository.PersonRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of Spring's {@link UserDetailsService} for providing application users.
 */
@Service
public class ApplicationUserDetailsService implements UserDetailsService {

    private final PersonRepository personRepository;

    public ApplicationUserDetailsService(final PersonRepository personRepository) {
        if (personRepository == null) throw new NullPointerException();
        this.personRepository = personRepository;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        if (username == null) throw new NullPointerException();
        if (username.isBlank()) throw new IllegalArgumentException();

        return this.personRepository.findByLibraryId(username.strip())
                .map(person -> new ApplicationUserDetails(
                        person.getId(),
                        person.getLibraryId(),
                        person.getEmailAddress(),
                        person.getType(),
                        person.getPasswordHash()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
    }
}
