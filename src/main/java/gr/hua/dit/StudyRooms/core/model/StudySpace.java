package gr.hua.dit.StudyRooms.core.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "STUDY_SPACE")
public class StudySpace {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name="space_id")
    private String studySpaceId;

    @Column(name="name")
    private String name;  //ex. R1 (room 1) - S12 (seat 12)

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private StudySpaceType type;

    @Column(name="capacity")
    private Integer capacity;    //Todo for seat-> 1 or null

    @Column(name="available")
    private Boolean available;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ime schedule
    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    public StudySpace() {
    }

    public StudySpace(Long id, String studySpaceId, String name, StudySpaceType type,
                      Integer capacity, Boolean available, Instant createdAt) {
        this.id = id;
        this.studySpaceId = studySpaceId;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.available = available;
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

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
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
                ", available=" + available +
                ", createdAt=" + createdAt +
                '}';
    }
}
