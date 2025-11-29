package gr.hua.dit.StudyRooms.core.service.mapper;


import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert {@link StudySpace} to {@link StudySpaceView}
 */
@Component
public class StudySpaceMapper {
    public StudySpaceView convertStudySpaceToStudySpaceView(final StudySpace studySpace) {
        if (studySpace == null) {
            return null;
        }

        return new StudySpaceView(
                studySpace.getId(),
                studySpace.getType(),
                studySpace.getStudySpaceId(),
                studySpace.getName(),
                studySpace.getCapacity(),
                studySpace.getAvailable(),
                studySpace.getOpeningTime(),
                studySpace.getClosingTime()
        );
    }
}
