package gr.hua.dit.StudyRooms.core.port.impl;

import gr.hua.dit.StudyRooms.core.port.PhoneNumberPort;
import gr.hua.dit.StudyRooms.core.port.impl.dto.PhoneNumberValidationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Default implementation of {@link PhoneNumberPort}. It uses the NOC external service.
 */
@Service
public class PhoneNumberPortImpl implements PhoneNumberPort {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PhoneNumberPortImpl(
            final RestTemplate restTemplate,
            @Value("${app.api.base-url}") final String baseUrl
    ) {
        if (restTemplate == null) throw new NullPointerException();
        if (baseUrl == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public PhoneNumberValidationResult validate(final String rawPhoneNumber) {
        if (rawPhoneNumber == null) throw new NullPointerException();
        if (rawPhoneNumber.isBlank()) throw new IllegalArgumentException();

        final String url =
                baseUrl + "/api/v1/phone-numbers/" + rawPhoneNumber + "/validations";

        final ResponseEntity<PhoneNumberValidationResult> response =
                this.restTemplate.getForEntity(url, PhoneNumberValidationResult.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            final PhoneNumberValidationResult result = response.getBody();
            if (result == null) throw new NullPointerException();
            return result;
        }

        throw new RuntimeException(
                "External service responded with " + response.getStatusCode()
        );
    }
}