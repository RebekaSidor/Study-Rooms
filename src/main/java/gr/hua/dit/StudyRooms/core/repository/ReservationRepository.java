package gr.hua.dit.StudyRooms.core.repository;

import gr.hua.dit.StudyRooms.core.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


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

    // auto tha mas pei an yparxei krathsh apo ayton ton foithth poy epikalyptetai xronika
    boolean existsByStudentIdAndEndTimeAfterAndStartTimeBefore(
            String studentId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT r.studySpaceId AS room, COUNT(r) AS total FROM Reservation r GROUP BY r.studySpaceId")
    List<Object[]> countReservationsPerRoom();

    long countDistinctStudentIdByStartTimeAfter(LocalDateTime date);

    @Query("""
    SELECT COUNT(s) FROM StudySpace s
    WHERE s.type = 'ROOM'
    AND s.capacity <= (
        SELECT COUNT(r) FROM Reservation r
        WHERE r.studySpaceId = s.studySpaceId
        AND r.startTime BETWEEN :from AND :to
    )
    """)
    long countFullyBookedRooms(@Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);


    @Query("SELECT r.studySpaceId, COUNT(r) FROM Reservation r GROUP BY r.studySpaceId")
    List<Object[]> countReservationsGroupByStudySpaceId();

}
