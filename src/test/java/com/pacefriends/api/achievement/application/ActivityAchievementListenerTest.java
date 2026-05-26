package com.pacefriends.api.achievement.application;

import com.pacefriends.api.achievement.infrastructure.AchievementEntity;
import com.pacefriends.api.achievement.infrastructure.AchievementJpaRepository;
import com.pacefriends.api.challenge.domain.ActivityRepository;
import com.pacefriends.api.challenge.event.ActivityRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivityAchievementListenerTest {

    @Mock
    private AchievementUnlockService unlockService;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private AchievementJpaRepository achievementRepository;

    @InjectMocks
    private ActivityAchievementListener listener;

    @Test
    void firstActivity_unlocksFirstStep() {
        UUID userId = UUID.randomUUID();
        when(activityRepository.countByUserId(userId)).thenReturn(1L);
        when(achievementRepository.findByCriteriaType("ACTIVITIES_COUNT")).thenReturn(List.of());

        listener.onActivityRegistered(new ActivityRegisteredEvent(userId, UUID.randomUUID()));

        verify(unlockService).tryUnlock(userId, "primeiro-passo");
    }

    @Test
    void secondActivity_doesNotUnlockFirstStep() {
        UUID userId = UUID.randomUUID();
        when(activityRepository.countByUserId(userId)).thenReturn(2L);
        when(achievementRepository.findByCriteriaType("ACTIVITIES_COUNT")).thenReturn(List.of());

        listener.onActivityRegistered(new ActivityRegisteredEvent(userId, UUID.randomUUID()));

        verify(unlockService, never()).tryUnlock(userId, "primeiro-passo");
    }

    @Test
    void activityCountMatchesCriteria_unlocksCountAchievement() {
        UUID userId = UUID.randomUUID();
        when(activityRepository.countByUserId(userId)).thenReturn(5L);

        AchievementEntity runner = mock(AchievementEntity.class);
        when(runner.getSlug()).thenReturn("corredor-iniciante");
        when(runner.getCriteriaValue()).thenReturn(5);
        when(achievementRepository.findByCriteriaType("ACTIVITIES_COUNT")).thenReturn(List.of(runner));

        listener.onActivityRegistered(new ActivityRegisteredEvent(userId, UUID.randomUUID()));

        verify(unlockService).tryUnlock(userId, "corredor-iniciante");
    }

    @Test
    void activityCountBelowCriteria_doesNotUnlock() {
        UUID userId = UUID.randomUUID();
        when(activityRepository.countByUserId(userId)).thenReturn(3L);

        AchievementEntity runner = mock(AchievementEntity.class);
        when(runner.getSlug()).thenReturn("corredor-iniciante");
        when(runner.getCriteriaValue()).thenReturn(5);
        when(achievementRepository.findByCriteriaType("ACTIVITIES_COUNT")).thenReturn(List.of(runner));

        listener.onActivityRegistered(new ActivityRegisteredEvent(userId, UUID.randomUUID()));

        verify(unlockService, never()).tryUnlock(userId, "corredor-iniciante");
    }
}
