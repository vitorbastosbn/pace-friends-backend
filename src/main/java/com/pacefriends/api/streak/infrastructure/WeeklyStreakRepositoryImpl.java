package com.pacefriends.api.streak.infrastructure;

import com.pacefriends.api.streak.domain.WeeklyStreak;
import com.pacefriends.api.streak.domain.WeeklyStreakRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class WeeklyStreakRepositoryImpl implements WeeklyStreakRepository {

    private final WeeklyStreakJpaRepository jpaRepository;

    public WeeklyStreakRepositoryImpl(WeeklyStreakJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WeeklyStreak save(WeeklyStreak streak) {
        WeeklyStreakEntity entity = WeeklyStreakMapper.toEntity(streak);
        return WeeklyStreakMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<WeeklyStreak> findByUserIdAndWeekStart(UUID userId, LocalDate weekStart) {
        return jpaRepository.findByUserIdAndWeekStartDate(userId, weekStart)
                .map(WeeklyStreakMapper::toDomain);
    }

    @Override
    public List<WeeklyStreak> findByUserId(UUID userId) {
        return jpaRepository.findByUserIdOrderByWeekStartDateDesc(userId).stream()
                .map(WeeklyStreakMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<WeeklyStreak> findRecentByUserId(UUID userId) {
        return jpaRepository.findFirstByUserIdOrderByWeekStartDateDesc(userId)
                .map(WeeklyStreakMapper::toDomain);
    }
}
