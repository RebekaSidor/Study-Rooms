package gr.hua.dit.StudyRooms.core.port.impl.dto;

import gr.hua.dit.StudyRooms.core.model.PersonType;

//todo wrong pachage
/**
 * Lookup REsults DTO.
 */
public record LookupResult(
    String raw,
    boolean exists,
    String libraryId,
    PersonType type
)
{}
