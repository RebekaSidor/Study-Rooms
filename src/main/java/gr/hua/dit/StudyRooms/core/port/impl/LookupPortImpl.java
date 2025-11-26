package gr.hua.dit.StudyRooms.core.port.impl;

import gr.hua.dit.StudyRooms.config.RestApiClientConfig;
import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.port.LookupPort;

import gr.hua.dit.StudyRooms.core.port.impl.dto.LookupResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Default implementation of {@link LookupPort}. It uses the NOC external service.
 */

@Service
public class LookupPortImpl implements LookupPort {

    private final RestTemplate restTemplate;

    public LookupPortImpl(final RestTemplate restTemplate) {
        if (restTemplate == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<PersonType> lookup(final String libraryId) {
        if (libraryId == null) throw new NullPointerException();
        if(libraryId.isBlank()) throw new IllegalArgumentException();

        final String baseUrl = RestApiClientConfig.BASE_URL;
        final String url = baseUrl + "/api/v1/lookups/" + libraryId;
        final ResponseEntity<LookupResult> response = this.restTemplate.getForEntity(url, LookupResult.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            final LookupResult lookupResult = response.getBody();
            if (lookupResult == null) throw new NullPointerException();
            return Optional.ofNullable(lookupResult.type());
        }
        throw new RuntimeException("FExternal service responded with " + response.getStatusCode());
    }
}
