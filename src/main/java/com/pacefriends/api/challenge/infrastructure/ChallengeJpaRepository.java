package com.pacefriends.api.challenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ChallengeJpaRepository extends JpaRepository<ChallengeEntity, UUID> {

    List<ChallengeEntity> findByUserId(UUID userId);
}
