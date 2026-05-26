package com.pacefriends.api.achievement.infrastructure;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_achievements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"})
)
public class UserAchievementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "achievement_id", nullable = false)
    private UUID achievementId;

    @Column(name = "unlocked_at", nullable = false)
    private OffsetDateTime unlockedAt;

    @PrePersist
    protected void onCreate() {
        if (unlockedAt == null) {
            unlockedAt = OffsetDateTime.now();
        }
    }

    protected UserAchievementEntity() {
    }

    public UserAchievementEntity(UUID userId, UUID achievementId) {
        this.userId = userId;
        this.achievementId = achievementId;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getAchievementId() { return achievementId; }
    public OffsetDateTime getUnlockedAt() { return unlockedAt; }
}
