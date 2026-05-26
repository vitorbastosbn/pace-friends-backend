package com.pacefriends.api.challenge.infrastructure;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activities")
class ActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "distance_km", nullable = false, precision = 8, scale = 3)
    private BigDecimal distanceKm;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "pace_seconds_per_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal paceSecondsPerKm;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    protected ActivityEntity() {
    }

    ActivityEntity(UUID challengeId, UUID userId, BigDecimal distanceKm, Integer durationSeconds,
                   BigDecimal paceSecondsPerKm, LocalDate activityDate, String notes) {
        this.challengeId = challengeId;
        this.userId = userId;
        this.distanceKm = distanceKm;
        this.durationSeconds = durationSeconds;
        this.paceSecondsPerKm = paceSecondsPerKm;
        this.activityDate = activityDate;
        this.notes = notes;
    }

    UUID getId() {
        return id;
    }

    UUID getChallengeId() {
        return challengeId;
    }

    UUID getUserId() {
        return userId;
    }

    BigDecimal getDistanceKm() {
        return distanceKm;
    }

    Integer getDurationSeconds() {
        return durationSeconds;
    }

    BigDecimal getPaceSecondsPerKm() {
        return paceSecondsPerKm;
    }

    LocalDate getActivityDate() {
        return activityDate;
    }

    String getNotes() {
        return notes;
    }

    LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
