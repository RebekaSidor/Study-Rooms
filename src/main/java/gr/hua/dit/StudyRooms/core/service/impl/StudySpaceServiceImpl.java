package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.PersonType;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.model.StudySpaceType;
import gr.hua.dit.StudyRooms.core.repository.StudySpaceRepository;
import gr.hua.dit.StudyRooms.core.security.CurrentUser;
import gr.hua.dit.StudyRooms.core.security.CurrentUserProvider;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import gr.hua.dit.StudyRooms.core.service.mapper.StudySpaceMapper;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceResult;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.List;

/**
 * Default implementation of {@link StudySpaceService}.
 */
@Service
public class StudySpaceServiceImpl implements StudySpaceService {

    private final StudySpaceRepository studySpaceRepository;
    private final StudySpaceMapper studySpaceMapper;
    private final CurrentUserProvider currentUserProvider;

    public StudySpaceServiceImpl(final StudySpaceRepository studySpaceRepository,final StudySpaceMapper studySpaceMapper, final CurrentUserProvider currentUserProvider) {
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

    //create study space ~ html
    @Override
    public void createStudySpace(StudySpace space) {
        //Security-----------------------
        final CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        if (currentUser.type() != PersonType.LIB_STAFF) {
            throw new SecurityException("Only staff can create study spaces");
        }
        studySpaceRepository.save(space);
    }
}
