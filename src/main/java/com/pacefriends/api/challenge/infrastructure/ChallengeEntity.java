package com.pacefriends.api.challenge.infrastructure;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "challenges")
class ChallengeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "goal_distance_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal goalDistanceKm;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    protected ChallengeEntity() {
    }

    ChallengeEntity(UUID userId, String title, BigDecimal goalDistanceKm, LocalDate deadline, String status) {
        this.userId = userId;
        this.title = title;
        this.goalDistanceKm = goalDistanceKm;
        this.deadline = deadline;
        this.status = status;
    }

    UUID getId() {
        return id;
    }

    void setId(UUID id) {
        this.id = id;
    }

    UUID getUserId() {
        return userId;
    }

    String getTitle() {
        return title;
    }

    BigDecimal getGoalDistanceKm() {
        return goalDistanceKm;
    }

    LocalDate getDeadline() {
        return deadline;
    }

    String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = status;
    }

    LocalDateTime getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
