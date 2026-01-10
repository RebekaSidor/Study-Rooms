package gr.hua.dit.StudyRooms.core.service;

import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceResult;
import gr.hua.dit.StudyRooms.core.service.model.NextStudySpaceResponse;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import jakarta.validation.ValidationException;

import java.util.List;
import java.util.Map;

/**
 * Service (contract) for managing rooms/seats.
 */
public interface StudySpaceBusinessLogicService {

    CreateStudySpaceResult createStudySpace(final CreateStudySpaceRequest createStudySpace);
    List<StudySpaceView> getAllStudySpaces();
    StudySpace getStudySpaceById(String studySpaceId);

    void updateStudySpace(StudySpace studySpace);
    void createStudySpace(StudySpace space);

    long countAll();

    Map<String, List<StudySpaceView>> getRoomsAndSeats();

    void validateAndCreateStudySpace(StudySpace space) throws ValidationException;
    NextStudySpaceResponse getNextStudySpace(StudySpaceType type);
    void validateAndUpdateStudySpace(StudySpace existing, StudySpace updated) throws ValidationException;

    StudySpaceLists getAllStudySpacesGrouped();

    public record StudySpaceLists(List<StudySpaceView> rooms, List<StudySpaceView> seats) {}
}
