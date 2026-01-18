package gr.hua.dit.StudyRooms.core.port.impl.dto;

public record LibraryDirectionsDto(
        String origin,
        String destination,
        String directionsUrl,
        String provider,
        Integer distanceMeters,   // σε μέτρα
        Integer durationSeconds   // σε δευτερόλεπτα
) {}