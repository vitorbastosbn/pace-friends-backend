package com.pacefriends.api.friendchallenge.infrastructure;

import com.pacefriends.api.friendchallenge.domain.FriendChallengeCheckIn;
import com.pacefriends.api.friendchallenge.domain.FriendChallengeCheckInRepository;
import org.springframework.stereotype.Repository;

import com.pacefriends.api.friendchallenge.domain.exception.CheckInNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FriendChallengeCheckInRepositoryImpl implements FriendChallengeCheckInRepository {

    private final FriendChallengeCheckInJpaRepository jpaRepository;

    public FriendChallengeCheckInRepositoryImpl(FriendChallengeCheckInJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FriendChallengeCheckIn save(FriendChallengeCheckIn checkIn) {
        FriendChallengeCheckInEntity entity = new FriendChallengeCheckInEntity(
                checkIn.challengeId(),
                checkIn.userId(),
                checkIn.distanceKm(),
                checkIn.durationSeconds(),
                checkIn.paceSecondsPerKm(),
                checkIn.checkInDate(),
                checkIn.notes(),
                checkIn.status()
        );
        FriendChallengeCheckInEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<FriendChallengeCheckIn> findAllByChallengeId(UUID challengeId) {
        return jpaRepository.findAllByFriendChallengeId(challengeId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByChallengeIdAndUserIdAndDate(UUID challengeId, UUID userId, LocalDate date) {
        return jpaRepository.existsByFriendChallengeIdAndUserIdAndCheckInDate(challengeId, userId, date);
    }

    @Override
    public Optional<FriendChallengeCheckIn> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public FriendChallengeCheckIn updateStatus(UUID id, String status) {
        FriendChallengeCheckInEntity entity = jpaRepository.findById(id)
                .orElseThrow(CheckInNotFoundException::new);
        entity.setStatus(status);
        FriendChallengeCheckInEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    private FriendChallengeCheckIn toDomain(FriendChallengeCheckInEntity entity) {
        return new FriendChallengeCheckIn(
                entity.getId(),
                entity.getFriendChallengeId(),
                entity.getUserId(),
                entity.getDistanceKm(),
                entity.getDurationSeconds(),
                entity.getPaceSecondsPerKm(),
                entity.getCheckInDate(),
                entity.getNotes(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
