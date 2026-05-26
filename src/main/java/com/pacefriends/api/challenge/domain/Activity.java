package com.pacefriends.api.challenge.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Activity {

    private final UUID id;
    private final UUID challengeId;
    private final UUID userId;
    private final BigDecimal distanceKm;
    private final Integer durationSeconds;
    private final BigDecimal paceSecondsPerKm;
    private final LocalDate activityDate;
    private final String notes;
    private final LocalDateTime createdAt;

    private Activity(Builder builder) {
        this.id = builder.id;
        this.challengeId = builder.challengeId;
        this.userId = builder.userId;
        this.distanceKm = builder.distanceKm;
        this.durationSeconds = builder.durationSeconds;
        this.paceSecondsPerKm = builder.paceSecondsPerKm;
        this.activityDate = builder.activityDate;
        this.notes = builder.notes;
        this.createdAt = builder.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getChallengeId() {
        return challengeId;
    }

    public UUID getUserId() {
        return userId;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public BigDecimal getPaceSecondsPerKm() {
        return paceSecondsPerKm;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID challengeId;
        private UUID userId;
        private BigDecimal distanceKm;
        private Integer durationSeconds;
        private BigDecimal paceSecondsPerKm;
        private LocalDate activityDate;
        private String notes;
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder challengeId(UUID challengeId) {
            this.challengeId = challengeId;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder distanceKm(BigDecimal distanceKm) {
            this.distanceKm = distanceKm;
            return this;
        }

        public Builder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public Builder paceSecondsPerKm(BigDecimal paceSecondsPerKm) {
            this.paceSecondsPerKm = paceSecondsPerKm;
            return this;
        }

        public Builder activityDate(LocalDate activityDate) {
            this.activityDate = activityDate;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Activity build() {
            return new Activity(this);
        }
    }
}
