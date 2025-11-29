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

    @Column(name = "study_space_id")
    private String studySpaceId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    //TODO timeslot list

    public Reservation() {

    }

    public Reservation(Long id, String reservationId, String studentId, String studySpaceId, Instant createdAt) {
        this.id = id;
        this.reservationId = reservationId;
        this.studentId = studentId;
        this.studySpaceId = studySpaceId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudySpaceId() {
        return studySpaceId;
    }

    public void setStudySpaceId(String studySpaceId) {
        this.studySpaceId = studySpaceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

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
