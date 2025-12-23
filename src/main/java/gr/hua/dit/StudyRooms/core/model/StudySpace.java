package gr.hua.dit.StudyRooms.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.time.LocalTime;

/**
 * StudySpace entity.
 */
@Entity
@Table(
        name = "STUDY_SPACE",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_study_space_space_id", columnNames = "space_id"),
                @UniqueConstraint(name = "uk_study_space_name", columnNames = "name")
        },
        indexes = {
                @Index(name = "idx_study_space_type", columnList = "type"),
                @Index(name = "idx_study_space_capacity", columnList = "capacity")
        }
)
public class StudySpace {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @NotNull
    @NotBlank
    @Size(max = 20)
    @Column(name="space_id", nullable = false, length = 50)
    private String studySpaceId;

    @NotNull
    @NotBlank
    @Size(max = 20)
    @Column(name="name", nullable = false, length = 50)
    private String name;  //ex. R1 (room 1) - S12 (seat 12)

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private StudySpaceType type;

    @Column(name="capacity")
    private Integer capacity; //only for rooms

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotNull
    @Future
    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @NotNull
    @Future
    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    public StudySpace() {}

    public StudySpace(Long id, String studySpaceId, String name, StudySpaceType type,
                      Integer capacity, Boolean available, Instant createdAt) {
        this.id = id;
        this.studySpaceId = studySpaceId;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudySpaceId() {
        return studySpaceId;
    }

    public void setStudySpaceId(String studySpaceId) {
        this.studySpaceId = studySpaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StudySpaceType getType() {
        return type;
    }

    public void setType(StudySpaceType type) {
        this.type = type;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(LocalTime openingTime) {
        this.openingTime = openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(LocalTime closingTime) {
        this.closingTime = closingTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "StudySpace{" +
                "id=" + id +
                ", studySpaceId='" + studySpaceId + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", capacity=" + capacity +
                ", createdAt=" + createdAt +
                '}';
    }
}
