package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceResult;

public interface StudySpaceService {
    CreateStudySpaceResult createStudySpace(final CreateStudySpaceRequest createStudySpace);
}
