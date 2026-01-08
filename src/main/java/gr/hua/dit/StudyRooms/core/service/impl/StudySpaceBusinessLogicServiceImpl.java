package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.repository.StudySpaceRepository;
import gr.hua.dit.StudyRooms.core.security.CurrentUser;
import gr.hua.dit.StudyRooms.core.security.CurrentUserProvider;
import gr.hua.dit.StudyRooms.core.service.StudySpaceBusinessLogicService;
import gr.hua.dit.StudyRooms.core.service.mapper.StudySpaceMapper;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceResult;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link StudySpaceBusinessLogicService}.
 */
@Service
public class StudySpaceBusinessLogicServiceImpl implements StudySpaceBusinessLogicService {

    private final StudySpaceRepository studySpaceRepository;
    private final StudySpaceMapper studySpaceMapper;
    private final CurrentUserProvider currentUserProvider;

    public StudySpaceBusinessLogicServiceImpl(final StudySpaceRepository studySpaceRepository, final StudySpaceMapper studySpaceMapper, final CurrentUserProvider currentUserProvider) {
        if (studySpaceRepository == null) throw new NullPointerException();
        if (studySpaceMapper == null) throw new NullPointerException();
        if (currentUserProvider == null) throw new NullPointerException();

        this.studySpaceRepository = studySpaceRepository;
        this.studySpaceMapper = studySpaceMapper;
        this.currentUserProvider = currentUserProvider;
    }

/*create study space ~ APIs & JSON*/
    @Override
    public CreateStudySpaceResult createStudySpace(final CreateStudySpaceRequest request) {
        //Security-----------------------------------
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.LIB_STAFF) {
            throw new SecurityException("Only staff can create study spaces");
        }

        if (request == null) throw new NullPointerException();

        StudySpace studySpace = new StudySpace();
        studySpace.setStudySpaceId(request.studySpaceId());
        studySpace.setName(request.name());
        studySpace.setType(request.type());
        studySpace.setCapacity(request.capacity());
        studySpace.setOpeningTime(request.openingTime() != null ? request.openingTime() : LocalTime.of(8,0));
        studySpace.setClosingTime(request.closingTime() != null ? request.closingTime() : LocalTime.of(20,0));

        //save in DB
        studySpace = this.studySpaceRepository.save(studySpace);

        //convert to View
        final StudySpaceView studySpaceView = this.studySpaceMapper.convertStudySpaceToStudySpaceView(studySpace);

        return CreateStudySpaceResult.success(studySpaceView);
    }


    //get list of all study spaces
    @Override
    public List<StudySpaceView> getAllStudySpaces() {
        return studySpaceRepository.findAll()
                .stream()
                .map(studySpaceMapper::convertStudySpaceToStudySpaceView)
                .toList();
    }

    //call to repository to get a study space by its id
    @Override
    public StudySpace getStudySpaceById(String studySpaceId) {
        return studySpaceRepository.findByStudySpaceId(studySpaceId)
                .orElse(null);
    }

    //count how many study spaces there are
    @Override
    public long countAll() {
        return studySpaceRepository.count();
    }

    //update study space details ~ staff edit study space
    @Override
    public void updateStudySpace(StudySpace updatedSpace) {
        //Security---------------------------
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.LIB_STAFF) {
            throw new SecurityException("Only staff can update study spaces");
        }

        //find study space
        StudySpace existing = studySpaceRepository
                .findByStudySpaceId(updatedSpace.getStudySpaceId())
                .orElseThrow(() -> new IllegalArgumentException("Study space not found"));

        //update hours
        existing.setOpeningTime(updatedSpace.getOpeningTime());
        existing.setClosingTime(updatedSpace.getClosingTime());

        //update capacity if it's a room
        if (existing.getType() == StudySpaceType.ROOM && updatedSpace.getCapacity() != null) {
            existing.setCapacity(updatedSpace.getCapacity());
        }

        studySpaceRepository.save(existing);
    }

    //create study space ~ HTML
    @Override
    public void createStudySpace(StudySpace space) {
        //Security-----------------------
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.LIB_STAFF) {
            throw new SecurityException("Only staff can create study spaces");
        }
        studySpaceRepository.save(space);
    }

    //show available study spaces for guest user
    public Map<String, List<StudySpaceView>> getRoomsAndSeats() {
        List<StudySpaceView> all = getAllStudySpaces();

        List<StudySpaceView> rooms = all.stream()
                .filter(s -> s.type().name().equals("ROOM"))
                .toList();

        List<StudySpaceView> seats = all.stream()
                .filter(s -> s.type().name().equals("SEAT"))
                .toList();

        Map<String, List<StudySpaceView>> result = new HashMap<>();
        result.put("rooms", rooms);
        result.put("seats", seats);

        return result;
    }

    public void validateAndUpdateStudySpace(StudySpace existing, StudySpace updated) throws ValidationException {
        // keep existing fields if not changed
        if (updated.getOpeningTime() != null) existing.setOpeningTime(updated.getOpeningTime());
        if (updated.getClosingTime() != null) existing.setClosingTime(updated.getClosingTime());
        if (existing.getType() == StudySpaceType.ROOM && updated.getCapacity() != null) {
            existing.setCapacity(updated.getCapacity());
        }

        LocalTime earliest = LocalTime.of(8, 0);
        LocalTime latest = LocalTime.of(22, 0);

        if (existing.getOpeningTime() != null && existing.getClosingTime() != null) {
            if (existing.getClosingTime().isBefore(existing.getOpeningTime())) {
                throw new ValidationException("Closing time cannot be before opening time!");
            }
            if (existing.getOpeningTime().isBefore(earliest) || existing.getClosingTime().isAfter(latest)) {
                throw new ValidationException("Time must be between 08:00 and 22:00!");
            }
        }

        // save to repository
        studySpaceRepository.save(existing);
    }


}
