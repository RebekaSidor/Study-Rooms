package gr.hua.dit.StudyRooms.core.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "RESERVATION")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name="reservation_id")
    private String reservationId;

    @Column(name = "student_id")
    private String studentId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "timeslot")
    private String timeslot;

    // Σύνδεση με StudySpace
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_space_id", referencedColumnName = "id")
    private StudySpace studySpace;

    public Reservation() {

    }

    public Reservation(Long id, String reservationId, String studentId, String studySpaceId, Instant createdAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.studentId = studentId;

        this.createdAt = createdAt;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getTimeslot() { return timeslot; }
    public void setTimeslot(String timeslot) { this.timeslot = timeslot; }

    public StudySpace getStudySpace() { return studySpace; }
    public void setStudySpace(StudySpace studySpace) { this.studySpace = studySpace; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", reservationId='" + reservationId + '\'' +
                ", studentId='" + studentId + '\'' +

                ", createdAt=" + createdAt +
                '}';
    }
}
