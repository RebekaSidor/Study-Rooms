package gr.hua.dit.StudyRooms.core.service.model;

import java.time.LocalDateTime;

public record StudentStatus(
        long absences,
        boolean hasPenalty,
        LocalDateTime penaltyUntil
) {}