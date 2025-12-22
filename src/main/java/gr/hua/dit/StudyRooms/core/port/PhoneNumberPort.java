package gr.hua.dit.StudyRooms.core.port;

import gr.hua.dit.StudyRooms.core.port.impl.dto.PhoneNumberValidationResult;

public interface PhoneNumberPort {
    PhoneNumberValidationResult validate(final String rawPhoneNumber);
}
