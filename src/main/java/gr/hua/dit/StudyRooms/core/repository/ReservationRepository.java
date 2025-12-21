package gr.hua.dit.StudyRooms.core.repository;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>{

    List<Reservation> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime now);
    List<Reservation> findByStudentId(String studentId);
    List<Reservation> findByStudentIdAndStartTimeBetween(String studentId, LocalDateTime start, LocalDateTime end);
    List<Reservation> findAllByOrderByStartTimeDesc();

    //check overlap
    boolean existsByStudySpaceIdAndEndTimeAfterAndStartTimeBefore(
            String studySpaceId,
            LocalDateTime start,
            LocalDateTime end
    );

    //check for other reservation at the same time
    boolean existsByStudentIdAndEndTimeAfterAndStartTimeBefore(
            String studentId,
            LocalDateTime start,
            LocalDateTime end
    );

    long countDistinctStudentIdByStartTimeAfter(LocalDateTime date);

    @Query("SELECT r.studySpaceId, COUNT(r) FROM Reservation r GROUP BY r.studySpaceId")
    List<Object[]> countReservationsGroupByStudySpaceId();

}
