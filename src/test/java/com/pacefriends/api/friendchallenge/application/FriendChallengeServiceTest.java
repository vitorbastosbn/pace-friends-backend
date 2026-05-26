package com.pacefriends.api.friendchallenge.application;

import com.pacefriends.api.friendchallenge.domain.*;
import com.pacefriends.api.friendchallenge.domain.exception.*;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeParticipantEntity;
import com.pacefriends.api.friendchallenge.infrastructure.FriendChallengeParticipantJpaRepository;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendChallengeServiceTest {

    @Mock
    private FriendChallengeRepository friendChallengeRepository;

    @Mock
    private FriendChallengeCheckInRepository checkInRepository;

    @Mock
    private FriendChallengeParticipantJpaRepository participantJpaRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendChallengeService service;

    private final UUID creatorId = UUID.randomUUID();
    private final UUID challengeId = UUID.randomUUID();
    private final LocalDate today = LocalDate.now();
    private final LocalDate tomorrow = today.plusDays(1);
    private final LocalDate nextMonth = today.plusDays(30);

    private FriendChallenge buildChallenge(UUID id, String status) {
        return buildChallenge(id, status, nextMonth);
    }

    private FriendChallenge buildChallenge(UUID id, String status, LocalDate endDate) {
        return new FriendChallenge(
                id, creatorId, "Desafio Teste", null,
                ChallengeType.DISTANCE, BigDecimal.valueOf(50),
                tomorrow, endDate, "ABCD1234", status,
                1, 5, ParticipantRole.CREATOR, OffsetDateTime.now(), List.of()
        );
    }

    // --- createChallenge ---

    @Test
    void createChallenge_happyPath_returnsChallengeWithCreatorRole() {
        FriendChallenge saved = buildChallenge(challengeId, "ACTIVE");
        when(friendChallengeRepository.existsByInviteCode(anyString())).thenReturn(false);
        when(friendChallengeRepository.save(any())).thenReturn(saved);
        when(participantJpaRepository.save(any())).thenReturn(mock(FriendChallengeParticipantEntity.class));
        when(participantJpaRepository.countByFriendChallengeId(challengeId)).thenReturn(1);

        FriendChallenge result = service.createChallenge(
                creatorId, "Desafio Teste", null,
                ChallengeType.DISTANCE, BigDecimal.valueOf(50),
                tomorrow, nextMonth
        );

        assertThat(result.myRole()).isEqualTo(ParticipantRole.CREATOR);
        assertThat(result.participantCount()).isEqualTo(1);
        verify(participantJpaRepository).save(any(FriendChallengeParticipantEntity.class));
    }

    @Test
    void createChallenge_startDateInPast_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                service.createChallenge(creatorId, "Test", null,
                        ChallengeType.DISTANCE, BigDecimal.valueOf(10),
                        today.minusDays(1), nextMonth)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("inicio");
    }

    @Test
    void createChallenge_endDateBeforeStartDate_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                service.createChallenge(creatorId, "Test", null,
                        ChallengeType.DISTANCE, BigDecimal.valueOf(10),
                        nextMonth, tomorrow)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("fim");
    }

    @Test
    void createChallenge_distanceTypeWithNullGoal_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                service.createChallenge(creatorId, "Test", null,
                        ChallengeType.DISTANCE, null,
                        tomorrow, nextMonth)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Meta");
    }

    @Test
    void createChallenge_paceTypeWithNullGoal_succeeds() {
        FriendChallenge saved = new FriendChallenge(
                challengeId, creatorId, "Pace Test", null,
                ChallengeType.PACE, null,
                tomorrow, nextMonth, "PACE1234", "ACTIVE",
                1, 5, ParticipantRole.CREATOR, OffsetDateTime.now(), List.of()
        );
        when(friendChallengeRepository.existsByInviteCode(anyString())).thenReturn(false);
        when(friendChallengeRepository.save(any())).thenReturn(saved);
        when(participantJpaRepository.save(any())).thenReturn(mock(FriendChallengeParticipantEntity.class));
        when(participantJpaRepository.countByFriendChallengeId(challengeId)).thenReturn(1);

        FriendChallenge result = service.createChallenge(
                creatorId, "Pace Test", null,
                ChallengeType.PACE, null, tomorrow, nextMonth
        );

        assertThat(result.challengeType()).isEqualTo(ChallengeType.PACE);
    }

    // --- joinChallenge ---

    @Test
    void joinChallenge_happyPath_returnsChallengeWithMemberRole() {
        UUID joinerId = UUID.randomUUID();
        FriendChallenge challenge = buildChallenge(challengeId, "ACTIVE");
        when(friendChallengeRepository.findByInviteCode("ABCD1234")).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.countByFriendChallengeId(challengeId)).thenReturn(1).thenReturn(2);
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, joinerId)).thenReturn(false);
        when(participantJpaRepository.save(any())).thenReturn(mock(FriendChallengeParticipantEntity.class));

        FriendChallenge result = service.joinChallenge(joinerId, "ABCD1234");

        assertThat(result.myRole()).isEqualTo(ParticipantRole.MEMBER);
        assertThat(result.participantCount()).isEqualTo(2);
    }

    @Test
    void joinChallenge_invalidCode_throwsInvalidInviteCodeException() {
        when(friendChallengeRepository.findByInviteCode("INVALID1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.joinChallenge(UUID.randomUUID(), "INVALID1"))
                .isInstanceOf(InvalidInviteCodeException.class);
    }

    @Test
    void joinChallenge_challengeNotActive_throwsChallengeNotActiveException() {
        FriendChallenge challenge = buildChallenge(challengeId, "COMPLETED");
        when(friendChallengeRepository.findByInviteCode("ABCD1234")).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> service.joinChallenge(UUID.randomUUID(), "ABCD1234"))
                .isInstanceOf(ChallengeNotActiveException.class);
    }

    @Test
    void joinChallenge_onAuditDate_transitionsAndRejectsJoin() {
        FriendChallenge challenge = buildChallenge(challengeId, FriendChallenge.STATUS_ACTIVE, today);
        when(friendChallengeRepository.findByInviteCode("ABCD1234")).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> service.joinChallenge(UUID.randomUUID(), "ABCD1234"))
                .isInstanceOf(ChallengeNotActiveException.class);
        verify(friendChallengeRepository).updateStatus(challengeId, FriendChallenge.STATUS_AUDIT);
    }

    @Test
    void joinChallenge_challengeFull_throwsChallengeFullException() {
        FriendChallenge challenge = buildChallenge(challengeId, "ACTIVE");
        when(friendChallengeRepository.findByInviteCode("ABCD1234")).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.countByFriendChallengeId(challengeId)).thenReturn(5);

        assertThatThrownBy(() -> service.joinChallenge(UUID.randomUUID(), "ABCD1234"))
                .isInstanceOf(ChallengeFullException.class);
    }

    @Test
    void joinChallenge_alreadyParticipant_throwsAlreadyParticipantException() {
        UUID joinerId = UUID.randomUUID();
        FriendChallenge challenge = buildChallenge(challengeId, "ACTIVE");
        when(friendChallengeRepository.findByInviteCode("ABCD1234")).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.countByFriendChallengeId(challengeId)).thenReturn(1);
        when(participantJpaRepository.existsByFriendChallengeIdAndUserId(challengeId, joinerId)).thenReturn(true);

        assertThatThrownBy(() -> service.joinChallenge(joinerId, "ABCD1234"))
                .isInstanceOf(AlreadyParticipantException.class);
    }

    // --- leaveChallenge ---

    @Test
    void leaveChallenge_memberInActiveChallenge_removesMembershipAndCheckIns() {
        UUID memberId = UUID.randomUUID();
        FriendChallenge challenge = buildChallenge(challengeId, FriendChallenge.STATUS_ACTIVE);
        FriendChallengeParticipantEntity participation = mock(FriendChallengeParticipantEntity.class);
        when(participation.getRole()).thenReturn(ParticipantRole.MEMBER.name());
        when(friendChallengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.findByFriendChallengeIdAndUserId(challengeId, memberId))
                .thenReturn(Optional.of(participation));

        service.leaveChallenge(memberId, challengeId);

        verify(checkInRepository).updateStatusByChallengeIdAndUserId(
                challengeId, memberId, FriendChallengeCheckIn.STATUS_REMOVED_BY_LEAVE);
        verify(participantJpaRepository).deleteByFriendChallengeIdAndUserId(challengeId, memberId);
    }

    @Test
    void leaveChallenge_creator_isDenied() {
        FriendChallenge challenge = buildChallenge(challengeId, FriendChallenge.STATUS_ACTIVE);
        FriendChallengeParticipantEntity participation = mock(FriendChallengeParticipantEntity.class);
        when(participation.getRole()).thenReturn(ParticipantRole.CREATOR.name());
        when(friendChallengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.findByFriendChallengeIdAndUserId(challengeId, creatorId))
                .thenReturn(Optional.of(participation));

        assertThatThrownBy(() -> service.leaveChallenge(creatorId, challengeId))
                .isInstanceOf(FriendChallengeAccessDeniedException.class);
        verify(checkInRepository, never()).updateStatusByChallengeIdAndUserId(any(), any(), any());
    }

    @Test
    void leaveChallenge_afterAudit_isRejectedWithoutRemovingParticipation() {
        FriendChallenge challenge = buildChallenge(challengeId, FriendChallenge.STATUS_AUDIT);
        when(friendChallengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));

        assertThatThrownBy(() -> service.leaveChallenge(UUID.randomUUID(), challengeId))
                .isInstanceOf(ChallengeNotActiveException.class);
        verifyNoInteractions(checkInRepository);
        verify(participantJpaRepository, never()).deleteByFriendChallengeIdAndUserId(any(), any());
    }

    // --- deleteChallenge ---

    @Test
    void deleteChallenge_creator_marksChallengeAndCheckInsDeleted() {
        when(friendChallengeRepository.findById(challengeId))
                .thenReturn(Optional.of(buildChallenge(challengeId, FriendChallenge.STATUS_ACTIVE)));

        service.deleteChallenge(creatorId, challengeId);

        verify(friendChallengeRepository).updateStatus(challengeId, FriendChallenge.STATUS_DELETED);
        verify(checkInRepository).updateStatusByChallengeId(
                challengeId, FriendChallengeCheckIn.STATUS_REMOVED_BY_DELETE);
    }

    @Test
    void deleteChallenge_member_isDenied() {
        when(friendChallengeRepository.findById(challengeId))
                .thenReturn(Optional.of(buildChallenge(challengeId, FriendChallenge.STATUS_ACTIVE)));

        assertThatThrownBy(() -> service.deleteChallenge(UUID.randomUUID(), challengeId))
                .isInstanceOf(FriendChallengeAccessDeniedException.class);
        verify(friendChallengeRepository, never()).updateStatus(challengeId, FriendChallenge.STATUS_DELETED);
        verifyNoInteractions(checkInRepository);
    }

    @Test
    void deleteChallenge_alreadyDeleted_isNotAccessible() {
        when(friendChallengeRepository.findById(challengeId))
                .thenReturn(Optional.of(buildChallenge(challengeId, FriendChallenge.STATUS_DELETED)));

        assertThatThrownBy(() -> service.deleteChallenge(creatorId, challengeId))
                .isInstanceOf(FriendChallengeNotFoundException.class);
        verify(friendChallengeRepository, never()).updateStatus(any(), anyString());
        verifyNoInteractions(checkInRepository);
    }

    // --- getChallengeDetail ---

    @Test
    void getChallengeDetail_nonParticipant_throwsAccessDeniedException() {
        UUID stranger = UUID.randomUUID();
        FriendChallenge challenge = buildChallenge(challengeId, "ACTIVE");
        when(friendChallengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(participantJpaRepository.findAllByFriendChallengeId(challengeId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getChallengeDetail(stranger, challengeId))
                .isInstanceOf(FriendChallengeAccessDeniedException.class);
    }

    @Test
    void getChallengeDetail_notFound_throwsFriendChallengeNotFoundException() {
        when(friendChallengeRepository.findById(challengeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getChallengeDetail(creatorId, challengeId))
                .isInstanceOf(FriendChallengeNotFoundException.class);
    }

    @Test
    void getChallengeDetail_deletedChallenge_isNotAccessible() {
        when(friendChallengeRepository.findById(challengeId))
                .thenReturn(Optional.of(buildChallenge(challengeId, FriendChallenge.STATUS_DELETED)));

        assertThatThrownBy(() -> service.getChallengeDetail(creatorId, challengeId))
                .isInstanceOf(FriendChallengeNotFoundException.class);
        verifyNoInteractions(participantJpaRepository);
    }

    @Test
    void getChallengeDetail_participant_returnsDetailWithParticipants() {
        FriendChallenge challenge = buildChallenge(challengeId, "ACTIVE");
        when(friendChallengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));

        FriendChallengeParticipantEntity participantEntity = mock(FriendChallengeParticipantEntity.class);
        when(participantEntity.getUserId()).thenReturn(creatorId);
        when(participantEntity.getRole()).thenReturn("CREATOR");
        when(participantEntity.getJoinedAt()).thenReturn(OffsetDateTime.now());
        when(participantJpaRepository.findAllByFriendChallengeId(challengeId))
                .thenReturn(List.of(participantEntity));

        User user = mock(User.class);
        when(user.getName()).thenReturn("Joao");
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(user));

        FriendChallengeDetailView result = service.getChallengeDetail(creatorId, challengeId);

        assertThat(result.participants()).hasSize(1);
        assertThat(result.participants().get(0).name()).isEqualTo("Joao");
        assertThat(result.challenge().myRole()).isEqualTo(ParticipantRole.CREATOR);
    }

    @Test
    void listChallenges_onAuditDate_returnsAuditStatus() {
        FriendChallenge challenge = buildChallenge(challengeId, FriendChallenge.STATUS_ACTIVE, today);
        when(friendChallengeRepository.findAllByUserId(creatorId)).thenReturn(List.of(challenge));

        List<FriendChallenge> result = service.listChallenges(creatorId);

        assertThat(result).singleElement()
                .extracting(FriendChallenge::status)
                .isEqualTo(FriendChallenge.STATUS_AUDIT);
        verify(friendChallengeRepository).updateStatus(challengeId, FriendChallenge.STATUS_AUDIT);
    }

    @Test
    void listChallenges_afterAudit_returnsFinishedStatus() {
        FriendChallenge challenge = buildChallenge(challengeId, FriendChallenge.STATUS_AUDIT, today.minusDays(1));
        when(friendChallengeRepository.findAllByUserId(creatorId)).thenReturn(List.of(challenge));

        List<FriendChallenge> result = service.listChallenges(creatorId);

        assertThat(result).singleElement()
                .extracting(FriendChallenge::status)
                .isEqualTo(FriendChallenge.STATUS_FINISHED);
        verify(friendChallengeRepository).updateStatus(challengeId, FriendChallenge.STATUS_FINISHED);
    }
}
