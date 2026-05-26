package com.pacefriends.api.achievement.application;

import com.pacefriends.api.achievement.infrastructure.AchievementEntity;
import com.pacefriends.api.achievement.infrastructure.AchievementJpaRepository;
import com.pacefriends.api.achievement.infrastructure.UserAchievementJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AchievementUnlockServiceTest {

    @Mock
    private AchievementJpaRepository achievementRepository;

    @Mock
    private UserAchievementJpaRepository userAchievementRepository;

    @InjectMocks
    private AchievementUnlockService service;

    @Test
    void tryUnlock_achievementFound_callsInsert() {
        UUID userId = UUID.randomUUID();
        UUID achievementId = UUID.randomUUID();
        AchievementEntity entity = buildAchievement(achievementId, "primeiro-passo");

        when(achievementRepository.findBySlug("primeiro-passo")).thenReturn(Optional.of(entity));

        service.tryUnlock(userId, "primeiro-passo");

        verify(userAchievementRepository).insertIfNotExists(userId, achievementId);
    }

    @Test
    void tryUnlock_idempotence_insertsEachCall() {
        UUID userId = UUID.randomUUID();
        UUID achievementId = UUID.randomUUID();
        AchievementEntity entity = buildAchievement(achievementId, "primeiro-passo");

        when(achievementRepository.findBySlug("primeiro-passo")).thenReturn(Optional.of(entity));
        doNothing().when(userAchievementRepository).insertIfNotExists(any(), any());

        service.tryUnlock(userId, "primeiro-passo");
        service.tryUnlock(userId, "primeiro-passo");

        verify(userAchievementRepository, times(2)).insertIfNotExists(userId, achievementId);
    }

    @Test
    void tryUnlock_achievementNotFound_doesNotCallInsert() {
        UUID userId = UUID.randomUUID();
        when(achievementRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

        service.tryUnlock(userId, "nonexistent");

        verify(userAchievementRepository, never()).insertIfNotExists(any(), any());
    }

    private AchievementEntity buildAchievement(UUID id, String slug) {
        AchievementEntity entity = mock(AchievementEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getSlug()).thenReturn(slug);
        return entity;
    }
}
