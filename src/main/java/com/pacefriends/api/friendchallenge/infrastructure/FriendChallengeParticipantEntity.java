package com.pacefriends.api.friendchallenge.infrastructure;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "friend_challenge_participants")
public class FriendChallengeParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "friend_challenge_id", nullable = false)
    private UUID friendChallengeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = OffsetDateTime.now();
    }

    protected FriendChallengeParticipantEntity() {
    }

    public FriendChallengeParticipantEntity(UUID friendChallengeId, UUID userId, String role) {
        this.friendChallengeId = friendChallengeId;
        this.userId = userId;
        this.role = role;
    }

    public UUID getId() { return id; }
    public UUID getFriendChallengeId() { return friendChallengeId; }
    public UUID getUserId() { return userId; }
    public String getRole() { return role; }
    public OffsetDateTime getJoinedAt() { return joinedAt; }

    public void setId(UUID id) { this.id = id; }
    public void setJoinedAt(OffsetDateTime joinedAt) { this.joinedAt = joinedAt; }
}
