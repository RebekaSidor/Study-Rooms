package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import java.util.List;

/**
 * Service for managing {@code StudySpace} for data analytics purposes.
 */
public interface StudySpaceDataService {
    List<StudySpaceView> getAllStudySpaces();
}
