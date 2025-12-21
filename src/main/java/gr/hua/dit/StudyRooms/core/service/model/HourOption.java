package gr.hua.dit.StudyRooms.core.service.model;

/**
 * HourOption (DTO)
 */
public record HourOption(
        String time,
        boolean available,
        boolean pastHour
) {}
