package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceResult;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;

import java.util.List;

public interface StudySpaceService {
    CreateStudySpaceResult createStudySpace(final CreateStudySpaceRequest createStudySpace);
    List<StudySpaceView> getAllStudySpaces();
    StudySpace getStudySpaceById(String studySpaceId);
    long countAll();

}
