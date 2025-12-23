package gr.hua.dit.StudyRooms.core.service.impl;

import gr.hua.dit.StudyRooms.core.model.StudySpace;
import gr.hua.dit.StudyRooms.core.repository.StudySpaceRepository;
import gr.hua.dit.StudyRooms.core.service.StudySpaceDataService;
import gr.hua.dit.StudyRooms.core.service.mapper.StudySpaceMapper;
import gr.hua.dit.StudyRooms.core.service.model.StudySpaceView;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Default implementation of {@link StudySpaceDataService}.
 */
@Service
public class StudySpaceDataServiceImpl implements StudySpaceDataService {
    private final StudySpaceRepository studySpaceRepository;
    private final StudySpaceMapper studySpaceMapper;
    public StudySpaceDataServiceImpl(StudySpaceRepository studySpaceRepository,
                                     StudySpaceMapper studySpaceMapper) {
        if (studySpaceRepository == null) throw new NullPointerException();
        if (studySpaceMapper == null) throw new NullPointerException();
        this.studySpaceRepository = studySpaceRepository;
        this.studySpaceMapper = studySpaceMapper;
    }

    @Override
    public List<StudySpaceView> getAllStudySpaces() {
        final List<StudySpace> studySpaceList = this.studySpaceRepository.findAll();
        final List<StudySpaceView> studySpaceViewList = studySpaceList
                .stream()
                .map(this.studySpaceMapper::convertStudySpaceToStudySpaceView)
                .toList();
        return studySpaceViewList;
    }
}