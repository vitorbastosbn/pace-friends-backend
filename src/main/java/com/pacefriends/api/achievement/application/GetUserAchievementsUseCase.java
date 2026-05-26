package com.pacefriends.api.achievement.application;

import com.pacefriends.api.achievement.domain.CriteriaType;
import com.pacefriends.api.achievement.infrastructure.AchievementEntity;
import com.pacefriends.api.achievement.infrastructure.AchievementJpaRepository;
import com.pacefriends.api.achievement.infrastructure.UserAchievementEntity;
import com.pacefriends.api.achievement.infrastructure.UserAchievementJpaRepository;
import com.pacefriends.api.challenge.domain.ActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetUserAchievementsUseCase {

    private final AchievementJpaRepository achievementRepository;
    private final UserAchievementJpaRepository userAchievementRepository;
    private final ActivityRepository activityRepository;

    public GetUserAchievementsUseCase(AchievementJpaRepository achievementRepository,
                                       UserAchievementJpaRepository userAchievementRepository,
                                       ActivityRepository activityRepository) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public List<AchievementView> execute(UUID userId) {
        List<AchievementEntity> catalog = achievementRepository.findAllByOrderByName();

        Map<UUID, UserAchievementEntity> unlockedById = userAchievementRepository.findByUserId(userId)
                .stream()
                .collect(Collectors.toMap(UserAchievementEntity::getAchievementId, ua -> ua));

        long activityCount = activityRepository.countByUserId(userId);

        return catalog.stream()
                .map(achievement -> buildView(achievement, unlockedById, activityCount))
                .toList();
    }

    private AchievementView buildView(AchievementEntity achievement,
                                       Map<UUID, UserAchievementEntity> unlockedById,
                                       long activityCount) {
        UserAchievementEntity ua = unlockedById.get(achievement.getId());
        boolean unlocked = ua != null;

        Integer progressCurrent = null;
        Integer progressTotal = null;

        if (!unlocked && CriteriaType.ACTIVITIES_COUNT.name().equals(achievement.getCriteriaType())
                && achievement.getCriteriaValue() != null) {
            progressCurrent = (int) Math.min(activityCount, achievement.getCriteriaValue());
            progressTotal = achievement.getCriteriaValue();
        }

        return new AchievementView(
                achievement.getId(),
                achievement.getSlug(),
                achievement.getName(),
                achievement.getDescription(),
                achievement.getIconKey(),
                unlocked,
                unlocked ? ua.getUnlockedAt() : null,
                progressCurrent,
                progressTotal
        );
    }
}
