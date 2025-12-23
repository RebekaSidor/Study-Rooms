package gr.hua.dit.StudyRooms.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotNull
    @NotBlank
    @Size(max = 20)
    @Column(name="reservation_id", nullable = false)
    private String reservationId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "library_id", nullable = false)
    private Person student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_space_id", referencedColumnName = "space_id", nullable = false)
    private StudySpace studySpace;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Future
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Future
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "present")
    private Boolean present;

    public Reservation() {}

    public Reservation(Long id, String reservationId, Person student, StudySpace studySpace, Instant createdAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.student = student;
        this.studySpace = studySpace;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public Person getStudent() {
        return student;
    }

    public void setStudent(Person student) {
        this.student = student;
    }

    public StudySpace getStudySpace() {
        return studySpace;
    }

    public void setStudySpace(StudySpace studySpace) {
        this.studySpace = studySpace;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getPresent() {
        return present;
    }

    public void setPresent(Boolean present) {
        this.present = present;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", reservationId='" + reservationId + '\'' +
                ", student=" + (student != null ? student.getId() : null) +
                ", studySpace=" + (studySpace != null ? studySpace.getId() : null) +
                ", createdAt=" + createdAt +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", present=" + present +
                '}';
    }

}
