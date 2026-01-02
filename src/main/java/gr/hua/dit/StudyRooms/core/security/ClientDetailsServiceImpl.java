package gr.hua.dit.StudyRooms.core.security;

import gr.hua.dit.StudyRooms.core.model.Client;
import gr.hua.dit.StudyRooms.core.repository.ClientRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Default implementation of {@link ClientDetailsService}.
 */
@Service
public class ClientDetailsServiceImpl implements ClientDetailsService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public ClientDetailsServiceImpl(final ClientRepository clientRepository,final PasswordEncoder passwordEncoder) {
        if (clientRepository == null) throw new NullPointerException();
        if (passwordEncoder == null) throw new NullPointerException();
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<ClientDetails> authenticate(final String id, final String secret) {
        if (id == null) throw new NullPointerException();
        if (id.isBlank()) throw new IllegalArgumentException();
        if (secret == null) throw new NullPointerException();
        if (secret.isBlank()) throw new IllegalArgumentException();

        return clientRepository.findByName(id)
                .filter(client -> passwordEncoder.matches(secret, client.getSecret()))
                .map(this::mapToClientDetails);
        }

    private ClientDetails mapToClientDetails(final Client client) {
        final Set<String> roles = parseRoles(client.getRolesCsv());

        return new ClientDetails(
                client.getName(),
                client.getSecret(),
                roles
        );
    }

    private Set<String> parseRoles(final String rolesCsv) {
        if (rolesCsv == null || rolesCsv.isBlank()) {
            return Collections.emptySet();
        }

        return Arrays.stream(rolesCsv.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

}