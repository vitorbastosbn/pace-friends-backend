package com.pacefriends.api.profile.infrastructure;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_settings")
class UserSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String objective;

    @Column(name = "weekly_frequency", nullable = false)
    private String weeklyFrequency;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

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

    protected UserSettingsEntity() {
    }

    UserSettingsEntity(UUID userId, String objective, String weeklyFrequency, LocalDate effectiveFrom) {
        this.userId = userId;
        this.objective = objective;
        this.weeklyFrequency = weeklyFrequency;
        this.effectiveFrom = effectiveFrom;
    }

    UUID getId() {
        return id;
    }

    UUID getUserId() {
        return userId;
    }

    String getObjective() {
        return objective;
    }

    String getWeeklyFrequency() {
        return weeklyFrequency;
    }

    LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    LocalDateTime getCreatedAt() {
        return createdAt;
    }

    LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
