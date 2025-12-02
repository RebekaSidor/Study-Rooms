package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.repository.StudySpaceRepository;
import gr.hua.dit.StudyRooms.core.service.PersonService;
import gr.hua.dit.StudyRooms.core.service.StudySpaceService;
import gr.hua.dit.StudyRooms.core.service.mapper.PersonMapper;
import gr.hua.dit.StudyRooms.core.service.mapper.StudySpaceMapper;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceRequest;
import gr.hua.dit.StudyRooms.core.service.model.CreateStudySpaceResult;
import gr.hua.dit.StudyRooms.core.service.model.PersonView;
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

    public StudySpaceServiceImpl(final StudySpaceRepository studySpaceRepository,final StudySpaceMapper studySpaceMapper) {
        if (studySpaceRepository == null) throw new NullPointerException();
        if (studySpaceMapper == null) throw new NullPointerException();

        this.studySpaceRepository = studySpaceRepository;
        this.studySpaceMapper = studySpaceMapper;
    }

    @Override
    public List<StudySpaceView> getAllStudySpaces() {
        return studySpaceRepository.findAll()
                .stream()
                .map(studySpaceMapper::convertStudySpaceToStudySpaceView)
                .toList();
    }


    @Override
    public CreateStudySpaceResult createStudySpace(final CreateStudySpaceRequest request) {
        if (request == null) throw new NullPointerException();


        StudySpace studySpace = new StudySpace();
        studySpace.setStudySpaceId(request.studySpaceId());
        studySpace.setName(request.name());
        studySpace.setType(request.type());
        studySpace.setCapacity(request.capacity());
        studySpace.setAvailable(request.available());
        studySpace.setOpeningTime(request.openingTime() != null ? request.openingTime() : LocalTime.of(8,0));
        studySpace.setClosingTime(request.closingTime() != null ? request.closingTime() : LocalTime.of(20,0));



        // Αποθήκευση στη βάση
        studySpace = this.studySpaceRepository.save(studySpace);

        // Μετατροπή σε View
        final StudySpaceView studySpaceView = this.studySpaceMapper.convertStudySpaceToStudySpaceView(studySpace);

        return CreateStudySpaceResult.success(studySpaceView);

    }
    @Override
    public StudySpace getStudySpaceById(String studySpaceId) {
        return studySpaceRepository.findByStudySpaceId(studySpaceId)
                .orElse(null); // ή throw exception αν θες
    }
    @Override
    public long countAll() {
        return studySpaceRepository.count();
    }

}
