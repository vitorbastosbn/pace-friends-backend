package com.pacefriends.api.streak.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class WeeklyStreak {

    private final UUID id;
    private final UUID userId;
    private final LocalDate weekStartDate;
    private final int targetFrequency;
    private final int daysCompleted;
    private final int streakCount;
    private final int xpEarned;
    private final StreakResult result;
    private final LocalDateTime processedAt;
    private final LocalDateTime createdAt;

    private WeeklyStreak(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.weekStartDate = builder.weekStartDate;
        this.targetFrequency = builder.targetFrequency;
        this.daysCompleted = builder.daysCompleted;
        this.streakCount = builder.streakCount;
        this.xpEarned = builder.xpEarned;
        this.result = builder.result;
        this.processedAt = builder.processedAt;
        this.createdAt = builder.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public int getTargetFrequency() {
        return targetFrequency;
    }

    public int getDaysCompleted() {
        return daysCompleted;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public StreakResult getResult() {
        return result;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
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
        private LocalDate weekStartDate;
        private int targetFrequency;
        private int daysCompleted;
        private int streakCount;
        private int xpEarned;
        private StreakResult result;
        private LocalDateTime processedAt;
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder weekStartDate(LocalDate weekStartDate) {
            this.weekStartDate = weekStartDate;
            return this;
        }

        public Builder targetFrequency(int targetFrequency) {
            this.targetFrequency = targetFrequency;
            return this;
        }

        public Builder daysCompleted(int daysCompleted) {
            this.daysCompleted = daysCompleted;
            return this;
        }

        public Builder streakCount(int streakCount) {
            this.streakCount = streakCount;
            return this;
        }

        public Builder xpEarned(int xpEarned) {
            this.xpEarned = xpEarned;
            return this;
        }

        public Builder result(StreakResult result) {
            this.result = result;
            return this;
        }

        public Builder processedAt(LocalDateTime processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public WeeklyStreak build() {
            return new WeeklyStreak(this);
        }
    }
}
