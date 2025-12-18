package gr.hua.dit.StudyRooms.core.service.model;

public record HourOption(
        String time,
        boolean available,
        boolean pastHour
) {}
