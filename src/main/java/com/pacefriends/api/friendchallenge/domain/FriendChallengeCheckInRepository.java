package com.pacefriends.api.friendchallenge.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendChallengeCheckInRepository {
    FriendChallengeCheckIn save(FriendChallengeCheckIn checkIn);
    List<FriendChallengeCheckIn> findAllByChallengeId(UUID challengeId);
    boolean existsValidByChallengeIdAndUserIdAndDate(UUID challengeId, UUID userId, LocalDate date);
    Optional<FriendChallengeCheckIn> findById(UUID id);
    FriendChallengeCheckIn updateStatus(UUID id, String status);
    void updateStatusByChallengeIdAndUserId(UUID challengeId, UUID userId, String status);
    void updateStatusByChallengeId(UUID challengeId, String status);
}
