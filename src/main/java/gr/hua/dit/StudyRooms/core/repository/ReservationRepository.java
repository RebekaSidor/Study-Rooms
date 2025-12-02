package gr.hua.dit.StudyRooms.core.repository;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>{

    // Βρες όλες τις κρατήσεις για συγκεκριμένο studySpaceId (String)
    List<Reservation> findByStudySpaceId(String studySpaceId);

    List<Reservation> findByStudentId(String studentId);

    // Έλεγχος overlap
    boolean existsByStudySpaceIdAndEndTimeAfterAndStartTimeBefore(
            String studySpaceId,
            LocalDateTime start,
            LocalDateTime end
    );

}
