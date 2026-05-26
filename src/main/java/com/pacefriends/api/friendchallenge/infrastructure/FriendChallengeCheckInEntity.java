package com.pacefriends.api.friendchallenge.infrastructure;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "friend_challenge_check_ins")
public class FriendChallengeCheckInEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "friend_challenge_id", nullable = false)
    private UUID friendChallengeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "distance_km", nullable = false, columnDefinition = "NUMERIC(10,3)")
    private double distanceKm;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "pace_seconds_per_km", nullable = false)
    private long paceSecondsPerKm;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    protected FriendChallengeCheckInEntity() {
    }

    public FriendChallengeCheckInEntity(UUID friendChallengeId, UUID userId, double distanceKm,
                                        int durationSeconds, long paceSecondsPerKm,
                                        LocalDate checkInDate, String notes, String status) {
        this.friendChallengeId = friendChallengeId;
        this.userId = userId;
        this.distanceKm = distanceKm;
        this.durationSeconds = durationSeconds;
        this.paceSecondsPerKm = paceSecondsPerKm;
        this.checkInDate = checkInDate;
        this.notes = notes;
        this.status = status;
    }

    public UUID getId() { return id; }
    public UUID getFriendChallengeId() { return friendChallengeId; }
    public UUID getUserId() { return userId; }
    public double getDistanceKm() { return distanceKm; }
    public int getDurationSeconds() { return durationSeconds; }
    public long getPaceSecondsPerKm() { return paceSecondsPerKm; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setId(UUID id) { this.id = id; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    void setStatus(String status) { this.status = status; }
}
