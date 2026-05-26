package com.pacefriends.api.friendchallenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendChallengeParticipantJpaRepository extends JpaRepository<FriendChallengeParticipantEntity, UUID> {
    List<FriendChallengeParticipantEntity> findAllByFriendChallengeId(UUID friendChallengeId);
    List<FriendChallengeParticipantEntity> findAllByUserId(UUID userId);
    boolean existsByFriendChallengeIdAndUserId(UUID friendChallengeId, UUID userId);
    int countByFriendChallengeId(UUID friendChallengeId);
    Optional<FriendChallengeParticipantEntity> findByFriendChallengeIdAndUserId(UUID friendChallengeId, UUID userId);
}
