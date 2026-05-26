package com.pacefriends.api.challenge.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Challenge {

    private final UUID id;
    private final UUID userId;
    private final String title;
    private final BigDecimal goalDistanceKm;
    private final LocalDate deadline;
    private final ChallengeStatus status;
    private final LocalDateTime createdAt;

    private Challenge(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.title = builder.title;
        this.goalDistanceKm = builder.goalDistanceKm;
        this.deadline = builder.deadline;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getGoalDistanceKm() {
        return goalDistanceKm;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private String title;
        private BigDecimal goalDistanceKm;
        private LocalDate deadline;
        private ChallengeStatus status;
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder goalDistanceKm(BigDecimal goalDistanceKm) {
            this.goalDistanceKm = goalDistanceKm;
            return this;
        }

        public Builder deadline(LocalDate deadline) {
            this.deadline = deadline;
            return this;
        }

        public Builder status(ChallengeStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Challenge build() {
            return new Challenge(this);
        }
    }
}
