package com.pacefriends.api.trail.application;

import com.pacefriends.api.trail.domain.*;
import com.pacefriends.api.trail.domain.exception.LevelUpNotAllowedException;
import com.pacefriends.api.trail.domain.exception.TrailAccessDeniedException;
import com.pacefriends.api.trail.infrastructure.TrailStreakRepository;
import com.pacefriends.api.trail.infrastructure.TrailUserRepository;
import com.pacefriends.api.trail.infrastructure.TrailUserStats;
import com.pacefriends.api.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrailServiceTest {

    @Mock
    private TrailUserRepository trailUserRepository;

    @Mock
    private TrailStreakRepository trailStreakRepository;

    @InjectMocks
    private TrailService trailService;

    private UUID userId;
    private UUID otherUserId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
    }

    // --- access control ---

    @Test
    void getTrainingPath_differentUser_throwsTrailAccessDeniedException() {
        assertThatThrownBy(() -> trailService.getTrainingPath(otherUserId, userId))
                .isInstanceOf(TrailAccessDeniedException.class);
    }

    @Test
    void levelUp_differentUser_throwsTrailAccessDeniedException() {
        assertThatThrownBy(() -> trailService.levelUp(otherUserId, userId))
                .isInstanceOf(TrailAccessDeniedException.class);
    }

    // --- getTrainingPath ---

    @Test
    void getTrainingPath_level1_noActivities_firstItemInProgress() {
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(0, 0.0, 0, 0, 1));
        when(trailStreakRepository.countCompletedStreakWeeks(userId)).thenReturn(0);

        TrainingPathData data = trailService.getTrainingPath(userId, userId);

        assertThat(data.getCurrentLevel()).isEqualTo(1);
        assertThat(data.getCurrentLevelName()).isEqualTo("Iniciante");
        assertThat(data.getPath().getItems().get(0).getStatus()).isEqualTo(ItemStatus.IN_PROGRESS);
        assertThat(data.getPath().getItems().get(1).getStatus()).isEqualTo(ItemStatus.LOCKED);
        assertThat(data.getPath().getCompletedItems()).isEqualTo(0);
        assertThat(data.isCanLevelUp()).isFalse();
    }

    @Test
    void getTrainingPath_level1_with1Activity_firstItemCompleted() {
        // Level 1: item1 needs 1 activity
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(1, 0.0, 0, 0, 1));
        when(trailStreakRepository.countCompletedStreakWeeks(userId)).thenReturn(0);

        TrainingPathData data = trailService.getTrainingPath(userId, userId);

        assertThat(data.getPath().getItems().get(0).getStatus()).isEqualTo(ItemStatus.COMPLETED);
        assertThat(data.getPath().getItems().get(1).getStatus()).isEqualTo(ItemStatus.IN_PROGRESS);
        assertThat(data.getPath().getCompletedItems()).isEqualTo(1);
    }

    @Test
    void getTrainingPath_allItemsCompleted_bonusXpNotYetAwarded_bonusXpAwardedFalse() {
        // All criteria met for level 1, but XP not yet high enough to include bonus
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(20, 50.0, 7, 540, 1));
        // total_xp=540 < 650 (0 threshold + 550 items + 100 bonus), so bonusXpAwarded=false
        when(trailStreakRepository.countCompletedStreakWeeks(userId)).thenReturn(0);

        TrainingPathData data = trailService.getTrainingPath(userId, userId);

        assertThat(data.getPath().getCompletedItems()).isEqualTo(10);
        assertThat(data.getPath().isBonusXpAwarded()).isFalse();
    }

    @Test
    void getTrainingPath_allItemsCompleted_bonusXpAlreadyInXp_bonusXpAwardedTrue() {
        // total_xp=650 >= 0+550+100, so bonusXpAwarded=true
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(20, 50.0, 7, 650, 1));
        when(trailStreakRepository.countCompletedStreakWeeks(userId)).thenReturn(4);

        TrainingPathData data = trailService.getTrainingPath(userId, userId);

        assertThat(data.getPath().isBonusXpAwarded()).isTrue();
        assertThat(data.isCanLevelUp()).isTrue();
    }

    @Test
    void getTrainingPath_canLevelUp_requiresAllConditions() {
        // path complete, streak=4, xp>=650
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(20, 50.0, 7, 650, 1));
        when(trailStreakRepository.countCompletedStreakWeeks(userId)).thenReturn(4);

        TrainingPathData data = trailService.getTrainingPath(userId, userId);

        assertThat(data.isCanLevelUp()).isTrue();
        assertThat(data.getNextLevelRequirements().getStreakWeeksCompleted()).isEqualTo(4);
        assertThat(data.getNextLevelRequirements().getXpRequired()).isEqualTo(650);
    }

    @Test
    void getTrainingPath_cannotLevelUp_streakInsufficient() {
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(20, 50.0, 7, 650, 1));
        when(trailStreakRepository.countCompletedStreakWeeks(userId)).thenReturn(2);

        TrainingPathData data = trailService.getTrainingPath(userId, userId);

        assertThat(data.isCanLevelUp()).isFalse();
    }

    @Test
    void getTrainingPath_level2_criteriaScaledByMultiplier() {
        // Level 2: item1 needs 2 activities, item3 needs 10km
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(2, 0.0, 0, 0, 2));
        when(trailStreakRepository.countCompletedStreakWeeks(userId)).thenReturn(0);

        TrainingPathData data = trailService.getTrainingPath(userId, userId);

        assertThat(data.getCurrentLevel()).isEqualTo(2);
        assertThat(data.getCurrentLevelName()).isEqualTo("Explorador");
        // item1: need 2 activities, have 2 -> COMPLETED
        assertThat(data.getPath().getItems().get(0).getStatus()).isEqualTo(ItemStatus.COMPLETED);
        // item2: need 6 activities, have 2 -> IN_PROGRESS
        assertThat(data.getPath().getItems().get(1).getStatus()).isEqualTo(ItemStatus.IN_PROGRESS);
    }

    // --- levelUp ---

    @Test
    void levelUp_allConditionsMet_incrementsLevel() {
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(20, 50.0, 7, 650, 1));
        when(trailStreakRepository.countCompletedStreakWeeks(userId)).thenReturn(4);

        User user = User.builder().googleId("g").email("e@e.com").name("N").build();
        when(trailUserRepository.findUser(userId)).thenReturn(user);
        when(trailUserRepository.saveUser(any())).thenReturn(user);

        LevelUpResult result = trailService.levelUp(userId, userId);

        assertThat(result.getPreviousLevel()).isEqualTo(1);
        assertThat(result.getNewLevel()).isEqualTo(2);
        assertThat(result.getNewLevelName()).isEqualTo("Explorador");
        verify(trailUserRepository).saveUser(any());
    }

    @Test
    void levelUp_pathNotComplete_throwsLevelUpNotAllowedException() {
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(0, 0.0, 0, 0, 1));

        assertThatThrownBy(() -> trailService.levelUp(userId, userId))
                .isInstanceOf(LevelUpNotAllowedException.class)
                .hasMessageContaining("trilha");
    }

    @Test
    void levelUp_streakInsufficient_throwsLevelUpNotAllowedException() {
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(20, 50.0, 7, 650, 1));
        when(trailStreakRepository.countCompletedStreakWeeks(userId)).thenReturn(2);

        assertThatThrownBy(() -> trailService.levelUp(userId, userId))
                .isInstanceOf(LevelUpNotAllowedException.class)
                .hasMessageContaining("ofensiva");
    }

    @Test
    void levelUp_xpInsufficient_throwsLevelUpNotAllowedException() {
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(20, 50.0, 7, 400, 1));
        when(trailStreakRepository.countCompletedStreakWeeks(userId)).thenReturn(4);

        assertThatThrownBy(() -> trailService.levelUp(userId, userId))
                .isInstanceOf(LevelUpNotAllowedException.class)
                .hasMessageContaining("XP");
    }

    @Test
    void levelUp_maxLevel_throwsLevelUpNotAllowedException() {
        when(trailUserRepository.getStats(userId))
                .thenReturn(new TrailUserStats(220, 550.0, 77, 10000, 11));

        assertThatThrownBy(() -> trailService.levelUp(userId, userId))
                .isInstanceOf(LevelUpNotAllowedException.class)
                .hasMessageContaining("maximo");
    }
}
