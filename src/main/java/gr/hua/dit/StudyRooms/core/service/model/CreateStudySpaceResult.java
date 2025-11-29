package gr.hua.dit.StudyRooms.core.service.model;

/**
 * CreateStudySpaceResult (DTO).
 */
public record CreateStudySpaceResult( boolean created, String reason, StudySpaceView studySpaceView) {
    public static CreateStudySpaceResult success(final StudySpaceView studySpaceView) {
        if (studySpaceView == null) throw new NullPointerException();
        return new CreateStudySpaceResult(true, null, studySpaceView);
    }

    public static CreateStudySpaceResult fail(final String reason) {
        if (reason == null) throw new NullPointerException();
        if (reason.isBlank()) throw new IllegalArgumentException();
        return new CreateStudySpaceResult(false, reason, null);
    }
}
