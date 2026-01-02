package gr.hua.dit.StudyRooms.core.port;

import gr.hua.dit.StudyRooms.core.port.impl.dto.PhoneNumberValidationResult;

/**
 * Port to external service for managing phone numbers.
 */
public interface PhoneNumberPort {
    PhoneNumberValidationResult validate(final String rawPhoneNumber);
}
