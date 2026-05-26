package com.pacefriends.api.achievement.application;

import com.pacefriends.api.friendchallenge.event.FriendChallengeFinishedEvent;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeCheckInEntity;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeCheckInJpaRepository;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeParticipantEntity;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeParticipantJpaRepository;
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
class FriendChallengeAchievementListenerTest {

    @Mock
    private AchievementUnlockService unlockService;

    @Mock
    private FriendChallengeParticipantJpaRepository participantRepository;

    @Mock
    private FriendChallengeCheckInJpaRepository checkInRepository;

    @InjectMocks
    private FriendChallengeAchievementListener listener;

    @Test
    void finished_allParticipantsGetCompetidor() {
        UUID challengeId = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        FriendChallengeParticipantEntity p1 = participantOf(user1);
        FriendChallengeParticipantEntity p2 = participantOf(user2);

        when(participantRepository.findAllByFriendChallengeId(challengeId))
                .thenReturn(List.of(p1, p2));
        when(checkInRepository.findAllByFriendChallengeId(challengeId)).thenReturn(List.of());

        listener.onFriendChallengeFinished(new FriendChallengeFinishedEvent(challengeId));

        verify(unlockService).tryUnlock(user1, "competidor");
        verify(unlockService).tryUnlock(user2, "competidor");
    }

    @Test
    void singleWinner_getsFirstVictory() {
        UUID challengeId = UUID.randomUUID();
        UUID winner = UUID.randomUUID();
        UUID loser = UUID.randomUUID();

        FriendChallengeParticipantEntity pw = participantOf(winner);
        FriendChallengeParticipantEntity pl = participantOf(loser);
        FriendChallengeCheckInEntity ciWinner = checkInOf(winner, 10.0, "VALID");
        FriendChallengeCheckInEntity ciLoser = checkInOf(loser, 5.0, "VALID");

        when(participantRepository.findAllByFriendChallengeId(challengeId))
                .thenReturn(List.of(pw, pl));
        when(checkInRepository.findAllByFriendChallengeId(challengeId))
                .thenReturn(List.of(ciWinner, ciLoser));

        listener.onFriendChallengeFinished(new FriendChallengeFinishedEvent(challengeId));

        verify(unlockService).tryUnlock(winner, "primeira-vitoria");
        verify(unlockService, never()).tryUnlock(loser, "primeira-vitoria");
    }

    @Test
    void tiedFirstPlace_bothGetFirstVictory() {
        UUID challengeId = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        FriendChallengeParticipantEntity p1 = participantOf(user1);
        FriendChallengeParticipantEntity p2 = participantOf(user2);
        FriendChallengeCheckInEntity ci1 = checkInOf(user1, 8.0, "VALID");
        FriendChallengeCheckInEntity ci2 = checkInOf(user2, 8.0, "VALID");

        when(participantRepository.findAllByFriendChallengeId(challengeId))
                .thenReturn(List.of(p1, p2));
        when(checkInRepository.findAllByFriendChallengeId(challengeId))
                .thenReturn(List.of(ci1, ci2));

        listener.onFriendChallengeFinished(new FriendChallengeFinishedEvent(challengeId));

        verify(unlockService).tryUnlock(user1, "primeira-vitoria");
        verify(unlockService).tryUnlock(user2, "primeira-vitoria");
    }

    @Test
    void noValidCheckIns_noFirstVictoryGranted() {
        UUID challengeId = UUID.randomUUID();
        UUID user = UUID.randomUUID();

        FriendChallengeParticipantEntity p = participantOf(user);
        FriendChallengeCheckInEntity ci = checkInOf(user, 5.0, "REJECTED");

        when(participantRepository.findAllByFriendChallengeId(challengeId))
                .thenReturn(List.of(p));
        when(checkInRepository.findAllByFriendChallengeId(challengeId))
                .thenReturn(List.of(ci));

        listener.onFriendChallengeFinished(new FriendChallengeFinishedEvent(challengeId));

        verify(unlockService, never()).tryUnlock(any(), eq("primeira-vitoria"));
    }

    private FriendChallengeParticipantEntity participantOf(UUID userId) {
        FriendChallengeParticipantEntity p = mock(FriendChallengeParticipantEntity.class);
        when(p.getUserId()).thenReturn(userId);
        return p;
    }

    private FriendChallengeCheckInEntity checkInOf(UUID userId, double distance, String status) {
        FriendChallengeCheckInEntity ci = mock(FriendChallengeCheckInEntity.class);
        when(ci.getUserId()).thenReturn(userId);
        when(ci.getDistanceKm()).thenReturn(distance);
        when(ci.getStatus()).thenReturn(status);
        return ci;
    }
}
