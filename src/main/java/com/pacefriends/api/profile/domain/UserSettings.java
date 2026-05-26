package com.pacefriends.api.profile.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserSettings {

    private final UUID id;
    private final UUID userId;
    private final UserObjective objective;
    private final WeeklyFrequency weeklyFrequency;
    private final LocalDate effectiveFrom;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private UserSettings(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.objective = builder.objective;
        this.weeklyFrequency = builder.weeklyFrequency;
        this.effectiveFrom = builder.effectiveFrom;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UserObjective getObjective() {
        return objective;
    }

    public WeeklyFrequency getWeeklyFrequency() {
        return weeklyFrequency;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID userId;
        private UserObjective objective;
        private WeeklyFrequency weeklyFrequency;
        private LocalDate effectiveFrom;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder objective(UserObjective objective) {
            this.objective = objective;
            return this;
        }

        public Builder weeklyFrequency(WeeklyFrequency weeklyFrequency) {
            this.weeklyFrequency = weeklyFrequency;
            return this;
        }

        public Builder effectiveFrom(LocalDate effectiveFrom) {
            this.effectiveFrom = effectiveFrom;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserSettings build() {
            return new UserSettings(this);
        }
    }
}
