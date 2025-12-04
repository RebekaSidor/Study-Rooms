package gr.hua.dit.StudyRooms.core.repository;

import gr.hua.dit.StudyRooms.core.model.StudySpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudySpaceRepository extends JpaRepository<StudySpace, Long> {

    Optional<StudySpace> findById(Long id);
    Optional<StudySpace> findByStudySpaceId(String studySpaceId);
}

