package gr.hua.dit.StudyRooms.core.repository;

import gr.hua.dit.StudyRooms.core.model.Person;
import gr.hua.dit.StudyRooms.core.model.Reservation;
import gr.hua.dit.StudyRooms.core.model.StudySpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>{

    List<Reservation> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime now);
    List<Reservation> findAllByOrderByStartTimeDesc();
    List<Reservation> findByStudent(Person student);
    List<Reservation> findByStudentAndStartTimeBetween(Person student, LocalDateTime start, LocalDateTime end);

    boolean existsByStudySpaceAndEndTimeAfterAndStartTimeBefore(
            StudySpace studySpace,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByStudentAndEndTimeAfterAndStartTimeBefore(
            Person student,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT COUNT(DISTINCT r.student.libraryId) FROM Reservation r WHERE r.startTime > :date")
    long countDistinctStudentsAfter(LocalDateTime date);


    @Query("SELECT r.studySpace.studySpaceId, COUNT(r) FROM Reservation r GROUP BY r.studySpace.studySpaceId")
    List<Object[]> countReservationsGroupByStudySpaceId();

}
