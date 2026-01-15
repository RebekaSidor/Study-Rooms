package gr.hua.dit.StudyRooms.web.rest.model;

public record CreateReservationRequest(
        Long studySpaceId,
        String date,
        String startTime,
        String endTime
) {}
