package com.pacefriends.api.challenge.infrastructure;

import com.pacefriends.api.challenge.domain.Challenge;
import com.pacefriends.api.challenge.domain.ChallengeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ChallengeRepositoryImpl implements ChallengeRepository {

    private final ChallengeJpaRepository jpaRepository;

    public ChallengeRepositoryImpl(ChallengeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Challenge save(Challenge challenge) {
        ChallengeEntity entity = ChallengeMapper.toEntity(challenge);
        return ChallengeMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Challenge> findById(UUID id) {
        return jpaRepository.findById(id).map(ChallengeMapper::toDomain);
    }

    @Override
    public List<Challenge> findAllByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(ChallengeMapper::toDomain)
                .toList();
    }
}
