package com.pacefriends.api.friendchallenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface FriendChallengeJpaRepository extends JpaRepository<FriendChallengeEntity, UUID> {
    Optional<FriendChallengeEntity> findByInviteCode(String code);
    boolean existsByInviteCode(String code);
}
