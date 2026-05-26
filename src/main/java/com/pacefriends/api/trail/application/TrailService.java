package com.pacefriends.api.trail.application;

import com.pacefriends.api.trail.domain.*;
import com.pacefriends.api.trail.domain.exception.LevelUpNotAllowedException;
import com.pacefriends.api.trail.domain.exception.TrailAccessDeniedException;
import com.pacefriends.api.trail.infrastructure.TrailStreakRepository;
import com.pacefriends.api.trail.infrastructure.TrailUserRepository;
import com.pacefriends.api.trail.infrastructure.TrailUserStats;
import com.pacefriends.api.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TrailService {

    private static final Logger log = LoggerFactory.getLogger(TrailService.class);
    private static final int STREAK_WEEKS_REQUIRED = 4;
    private static final int BONUS_XP = 100;
    private static final int ITEMS_XP_SUM = 550; // 10+20+30+40+50+60+70+80+90+100

    private final TrailUserRepository trailUserRepository;
    private final TrailStreakRepository trailStreakRepository;

    public TrailService(TrailUserRepository trailUserRepository,
                        TrailStreakRepository trailStreakRepository) {
        this.trailUserRepository = trailUserRepository;
        this.trailStreakRepository = trailStreakRepository;
    }

    @Transactional(readOnly = true)
    public TrainingPathData getTrainingPath(UUID requestingUserId, UUID targetUserId) {
        validateAccess(requestingUserId, targetUserId);

        TrailUserStats stats = trailUserRepository.getStats(targetUserId);
        int currentLevel = stats.getCurrentLevel();

        LevelDefinition levelDef = LevelCatalog.getLevel(currentLevel)
                .orElseThrow(() -> new IllegalStateException("Nivel invalido: " + currentLevel));

        List<TrainingPathItem> items = computeItems(levelDef, stats);
        int completedCount = (int) items.stream()
                .filter(i -> i.getStatus() == ItemStatus.COMPLETED)
                .count();

        boolean allCompleted = completedCount == levelDef.getItems().size();
        boolean bonusXpAwarded = allCompleted &&
                stats.getTotalXp() >= xpThresholdForLevel(currentLevel) + ITEMS_XP_SUM + BONUS_XP;

        TrainingPath path = new TrainingPath(
                currentLevel, completedCount, levelDef.getItems().size(),
                null, bonusXpAwarded, items);

        int streakWeeksCompleted = trailStreakRepository.countCompletedStreakWeeks(targetUserId);
        int xpRequired = currentLevel * 650;

        boolean canLevelUp = allCompleted
                && streakWeeksCompleted >= STREAK_WEEKS_REQUIRED
                && stats.getTotalXp() >= xpRequired
                && currentLevel < LevelCatalog.MAX_LEVEL;

        NextLevelRequirements nextLevelReqs = new NextLevelRequirements(
                allCompleted, STREAK_WEEKS_REQUIRED, streakWeeksCompleted,
                xpRequired, stats.getTotalXp());

        return new TrainingPathData(
                targetUserId, currentLevel, LevelCatalog.getLevelName(currentLevel),
                path, canLevelUp, nextLevelReqs);
    }

    @Transactional
    public LevelUpResult levelUp(UUID requestingUserId, UUID targetUserId) {
        validateAccess(requestingUserId, targetUserId);

        TrailUserStats stats = trailUserRepository.getStats(targetUserId);
        int currentLevel = stats.getCurrentLevel();

        if (currentLevel >= LevelCatalog.MAX_LEVEL) {
            throw new LevelUpNotAllowedException("Voce ja atingiu o nivel maximo.");
        }

        LevelDefinition levelDef = LevelCatalog.getLevel(currentLevel)
                .orElseThrow(() -> new IllegalStateException("Nivel invalido: " + currentLevel));

        List<TrainingPathItem> items = computeItems(levelDef, stats);
        boolean allCompleted = items.stream().allMatch(i -> i.getStatus() == ItemStatus.COMPLETED);

        if (!allCompleted) {
            throw new LevelUpNotAllowedException("A trilha atual nao foi completada.");
        }

        int streakWeeksCompleted = trailStreakRepository.countCompletedStreakWeeks(targetUserId);
        if (streakWeeksCompleted < STREAK_WEEKS_REQUIRED) {
            throw new LevelUpNotAllowedException(
                    String.format("Voce precisa de %d semanas de ofensiva concluidas. Voce tem %d.",
                            STREAK_WEEKS_REQUIRED, streakWeeksCompleted));
        }

        int xpRequired = currentLevel * 650;
        if (stats.getTotalXp() < xpRequired) {
            throw new LevelUpNotAllowedException(
                    String.format("XP insuficiente. Necessario: %d, atual: %d.",
                            xpRequired, stats.getTotalXp()));
        }

        User user = trailUserRepository.findUser(targetUserId);

        // Award bonus XP if not yet awarded
        boolean bonusAlreadyAwarded = stats.getTotalXp() >= xpThresholdForLevel(currentLevel) + ITEMS_XP_SUM + BONUS_XP;
        if (!bonusAlreadyAwarded) {
            user.addXp(BONUS_XP);
        }

        user.setCurrentLevel(currentLevel + 1);
        trailUserRepository.saveUser(user);

        log.debug("User {} leveled up from {} to {}", targetUserId, currentLevel, currentLevel + 1);

        return new LevelUpResult(currentLevel, currentLevel + 1,
                LevelCatalog.getLevelName(currentLevel + 1));
    }

    private List<TrainingPathItem> computeItems(LevelDefinition levelDef, TrailUserStats stats) {
        List<TrainingPathItemDefinition> definitions = levelDef.getItems();
        List<TrainingPathItem> items = new ArrayList<>();
        boolean foundInProgress = false;

        for (TrainingPathItemDefinition def : definitions) {
            boolean met = isCriterionMet(def, stats);
            ItemStatus status;
            if (met) {
                status = ItemStatus.COMPLETED;
            } else if (!foundInProgress) {
                status = ItemStatus.IN_PROGRESS;
                foundInProgress = true;
            } else {
                status = ItemStatus.LOCKED;
            }
            items.add(new TrainingPathItem(def.getPosition(), def.getDescription(),
                    def.getXpReward(), status, null));
        }
        return items;
    }

    private boolean isCriterionMet(TrainingPathItemDefinition def, TrailUserStats stats) {
        return switch (def.getCriterionType()) {
            case ACTIVITY_COUNT -> stats.getActivityCount() >= def.getCriterionValue();
            case TOTAL_DISTANCE_KM -> stats.getTotalDistanceKm() >= def.getCriterionValue();
            case STREAK_DAYS -> stats.getCurrentStreak() >= def.getCriterionValue();
        };
    }

    private void validateAccess(UUID requestingUserId, UUID targetUserId) {
        if (!requestingUserId.equals(targetUserId)) {
            throw new TrailAccessDeniedException();
        }
    }

    private int xpThresholdForLevel(int level) {
        return (level - 1) * 650;
    }
}
