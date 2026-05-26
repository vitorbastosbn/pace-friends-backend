package com.pacefriends.api.streak.infrastructure;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "weekly_streaks")
class WeeklyStreakEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "target_frequency", nullable = false)
    private int targetFrequency;

    @Column(name = "days_completed", nullable = false)
    private int daysCompleted;

    @Column(name = "streak_count", nullable = false)
    private int streakCount;

    @Column(name = "xp_earned", nullable = false)
    private int xpEarned;

    @Column(name = "result", nullable = false)
    private String result;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    protected WeeklyStreakEntity() {
    }

    WeeklyStreakEntity(UUID userId, LocalDate weekStartDate, int targetFrequency,
                       int daysCompleted, int streakCount, int xpEarned,
                       String result, LocalDateTime processedAt) {
        this.userId = userId;
        this.weekStartDate = weekStartDate;
        this.targetFrequency = targetFrequency;
        this.daysCompleted = daysCompleted;
        this.streakCount = streakCount;
        this.xpEarned = xpEarned;
        this.result = result;
        this.processedAt = processedAt;
    }

    UUID getId() {
        return id;
    }

    UUID getUserId() {
        return userId;
    }

    LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    int getTargetFrequency() {
        return targetFrequency;
    }

    int getDaysCompleted() {
        return daysCompleted;
    }

    int getStreakCount() {
        return streakCount;
    }

    int getXpEarned() {
        return xpEarned;
    }

    String getResult() {
        return result;
    }

    LocalDateTime getProcessedAt() {
        return processedAt;
    }

    LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
