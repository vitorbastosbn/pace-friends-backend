package com.pacefriends.api.challenge.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChallengeRepository {

    Challenge save(Challenge challenge);

    Optional<Challenge> findById(UUID id);

    List<Challenge> findAllByUserId(UUID userId);
}
