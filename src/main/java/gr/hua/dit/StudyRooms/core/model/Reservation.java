package gr.hua.dit.StudyRooms.core.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Reservation entity.
 */
@Entity
@Table(
        name = "RESERVATION",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_reservation_reservation_id",
                        columnNames = "reservation_id"
                )
        },
        indexes = {
                @Index(name = "idx_reservation_student", columnList = "student_id"),
                @Index(name = "idx_reservation_space", columnList = "study_space_id"),
                @Index(name = "idx_reservation_start_time", columnList = "start_time")
        }
)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(name="reservation_id", nullable = false)
    private String reservationId;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "study_space_id", nullable = false)
    private String studySpaceId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "present")
    private Boolean present;

    public Reservation() {}

    public Reservation(Long id, String reservationId, String studentId, String studySpaceId, Instant createdAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.studentId = studentId;
        this.studySpaceId = studySpaceId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudySpaceId() { return studySpaceId; }
    public void setStudySpaceId(String studySpaceId) { this.studySpaceId = studySpaceId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Boolean getPresent() {return present;}

    public void setPresent(Boolean present) {this.present = present;}

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", reservationId='" + reservationId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", studySpaceId='" + studySpaceId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
