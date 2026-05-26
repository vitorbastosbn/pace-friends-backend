package com.pacefriends.api.friendchallenge.infrastructure;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "friend_challenges")
class FriendChallengeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "challenge_type", nullable = false, length = 20)
    private String challengeType;

    @Column(name = "goal_value", precision = 10, scale = 2)
    private BigDecimal goalValue;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "invite_code", nullable = false, length = 8)
    private String inviteCode;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    protected FriendChallengeEntity() {
    }

    FriendChallengeEntity(UUID creatorId, String title, String description, String challengeType,
                          BigDecimal goalValue, LocalDate startDate, LocalDate endDate,
                          String inviteCode, String status) {
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.challengeType = challengeType;
        this.goalValue = goalValue;
        this.startDate = startDate;
        this.endDate = endDate;
        this.inviteCode = inviteCode;
        this.status = status;
    }

    UUID getId() { return id; }
    UUID getCreatorId() { return creatorId; }
    String getTitle() { return title; }
    String getDescription() { return description; }
    String getChallengeType() { return challengeType; }
    BigDecimal getGoalValue() { return goalValue; }
    LocalDate getStartDate() { return startDate; }
    LocalDate getEndDate() { return endDate; }
    String getInviteCode() { return inviteCode; }
    String getStatus() { return status; }
    OffsetDateTime getCreatedAt() { return createdAt; }

    void setId(UUID id) { this.id = id; }
    void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    void setStatus(String status) { this.status = status; }
}
