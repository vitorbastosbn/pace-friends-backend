package com.pacefriends.api.friendchallenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FriendChallengeCheckInJpaRepository extends JpaRepository<FriendChallengeCheckInEntity, UUID> {
    List<FriendChallengeCheckInEntity> findAllByFriendChallengeId(UUID friendChallengeId);
    boolean existsByFriendChallengeIdAndUserIdAndCheckInDate(UUID friendChallengeId, UUID userId, LocalDate checkInDate);
}
