package com.pacefriends.api.friendchallenge.infrastructure;

import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import com.pacefriends.api.friendchallenge.domain.FriendChallengeRepository;
import com.pacefriends.api.friendchallenge.domain.ParticipantRole;
import com.pacefriends.api.friendchallenge.domain.exception.FriendChallengeNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FriendChallengeRepositoryImpl implements FriendChallengeRepository {

    private final FriendChallengeJpaRepository jpaRepository;
    private final FriendChallengeParticipantJpaRepository participantJpaRepository;

    public FriendChallengeRepositoryImpl(FriendChallengeJpaRepository jpaRepository,
                                          FriendChallengeParticipantJpaRepository participantJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.participantJpaRepository = participantJpaRepository;
    }

    @Override
    public FriendChallenge save(FriendChallenge challenge) {
        FriendChallengeEntity entity = FriendChallengeMapper.toEntity(challenge);
        FriendChallengeEntity saved = jpaRepository.save(entity);
        int count = participantJpaRepository.countByFriendChallengeId(saved.getId());
        return FriendChallengeMapper.toDomain(saved, count, challenge.myRole());
    }

    @Override
    public Optional<FriendChallenge> findById(UUID id) {
        return jpaRepository.findById(id).map(entity -> {
            int count = participantJpaRepository.countByFriendChallengeId(entity.getId());
            return FriendChallengeMapper.toDomain(entity, count, null);
        });
    }

    @Override
    public Optional<FriendChallenge> findByInviteCode(String inviteCode) {
        return jpaRepository.findByInviteCode(inviteCode)
                .filter(entity -> !FriendChallenge.STATUS_DELETED.equals(entity.getStatus()))
                .map(entity -> {
            int count = participantJpaRepository.countByFriendChallengeId(entity.getId());
            return FriendChallengeMapper.toDomain(entity, count, null);
        });
    }

    @Override
    public boolean existsByInviteCode(String inviteCode) {
        return jpaRepository.existsByInviteCode(inviteCode);
    }

    @Override
    public List<FriendChallenge> findAllByUserId(UUID userId) {
        List<FriendChallengeParticipantEntity> participations =
                participantJpaRepository.findAllByUserId(userId);

        return participations.stream()
                .map(p -> jpaRepository.findById(p.getFriendChallengeId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(entity -> !FriendChallenge.STATUS_DELETED.equals(entity.getStatus()))
                .map(entity -> {
                    int count = participantJpaRepository.countByFriendChallengeId(entity.getId());
                    ParticipantRole myRole = participations.stream()
                            .filter(p -> p.getFriendChallengeId().equals(entity.getId()))
                            .findFirst()
                            .map(p -> ParticipantRole.valueOf(p.getRole()))
                            .orElse(ParticipantRole.MEMBER);
                    return FriendChallengeMapper.toDomain(entity, count, myRole);
                })
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .toList();
    }

    @Override
    public void updateStatus(UUID id, String status) {
        FriendChallengeEntity entity = jpaRepository.findById(id)
                .orElseThrow(() -> new FriendChallengeNotFoundException(id));
        entity.setStatus(status);
        jpaRepository.save(entity);
    }
}
