package com.pacefriends.api.challenge.infrastructure;

import com.pacefriends.api.challenge.domain.Activity;
import com.pacefriends.api.challenge.domain.ActivityRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public class ActivityRepositoryImpl implements ActivityRepository {

    private final ActivityJpaRepository jpaRepository;

    public ActivityRepositoryImpl(ActivityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Activity save(Activity activity) {
        ActivityEntity entity = ActivityMapper.toEntity(activity);
        return ActivityMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Activity> findAllByChallengeId(UUID challengeId) {
        return jpaRepository.findByChallengeIdOrderByActivityDateDesc(challengeId).stream()
                .map(ActivityMapper::toDomain)
                .toList();
    }

    @Override
    public BigDecimal sumDistanceByChallengeId(UUID challengeId) {
        return jpaRepository.sumDistanceByChallengeId(challengeId);
    }

    @Override
    public Set<LocalDate> findActivityDatesByUserInWeek(UUID userId, LocalDate weekStart, LocalDate weekEnd) {
        return jpaRepository.findDistinctActivityDatesByUserAndWeek(userId, weekStart, weekEnd);
    }
}
