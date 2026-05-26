package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.auth.JwtUtil;
import com.pacefriends.api.config.JacksonConfig;
import com.pacefriends.api.config.SecurityConfig;
import com.pacefriends.api.friendchallenge.application.FriendChallengeDetailView;
import com.pacefriends.api.friendchallenge.application.FriendChallengeService;
import com.pacefriends.api.friendchallenge.application.CheckInService;
import com.pacefriends.api.friendchallenge.application.RankingView;
import com.pacefriends.api.friendchallenge.domain.*;
import com.pacefriends.api.friendchallenge.domain.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendChallengeController.class)
@Import({SecurityConfig.class, JacksonConfig.class, JwtUtil.class})
@ActiveProfiles("test")
class FriendChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FriendChallengeService friendChallengeService;

    @MockBean
    private CheckInService checkInService;

    @MockBean
    private com.pacefriends.api.user.UserRepository userRepository;

    private final UUID userId = UUID.randomUUID();
    private final UUID challengeId = UUID.randomUUID();

    private FriendChallenge buildChallenge() {
        return new FriendChallenge(
                challengeId, userId, "Corrida de Maio", "Descricao",
                ChallengeType.DISTANCE, BigDecimal.valueOf(50.0),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30),
                "ABCD1234", "ACTIVE", 1, 5, ParticipantRole.CREATOR,
                OffsetDateTime.now(), List.of()
        );
    }

    private RequestPostProcessor authAs(UUID id) {
        return authentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        id, null, Collections.emptyList()));
    }

    // --- POST /api/v1/friend-challenges ---

    @Test
    void createChallenge_validRequest_returns201() throws Exception {
        when(friendChallengeService.createChallenge(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(buildChallenge());

        mockMvc.perform(post("/api/v1/friend-challenges")
                        .with(authAs(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Corrida de Maio",
                              "challengeType": "DISTANCE",
                              "goalValue": 50.0,
                              "startDate": "2026-05-26",
                              "endDate": "2026-06-25"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inviteCode").value("ABCD1234"))
                .andExpect(jsonPath("$.myRole").value("CREATOR"))
                .andExpect(jsonPath("$.maxParticipants").value(5));
    }

    @Test
    void createChallenge_missingTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/friend-challenges")
                        .with(authAs(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "challengeType": "DISTANCE",
                              "goalValue": 50.0,
                              "startDate": "2026-05-26",
                              "endDate": "2026-06-25"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    @Test
    void createChallenge_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/friend-challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // --- POST /api/v1/friend-challenges/join ---

    @Test
    void joinChallenge_validCode_returns200() throws Exception {
        FriendChallenge joined = new FriendChallenge(
                challengeId, userId, "Corrida de Maio", null,
                ChallengeType.DISTANCE, BigDecimal.valueOf(50.0),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30),
                "ABCD1234", "ACTIVE", 2, 5, ParticipantRole.MEMBER,
                OffsetDateTime.now(), List.of()
        );
        when(friendChallengeService.joinChallenge(any(), eq("ABCD1234"))).thenReturn(joined);

        mockMvc.perform(post("/api/v1/friend-challenges/join")
                        .with(authAs(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "inviteCode": "ABCD1234" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myRole").value("MEMBER"))
                .andExpect(jsonPath("$.participantCount").value(2));
    }

    @Test
    void joinChallenge_invalidCode_returns400() throws Exception {
        when(friendChallengeService.joinChallenge(any(), any()))
                .thenThrow(new InvalidInviteCodeException());

        mockMvc.perform(post("/api/v1/friend-challenges/join")
                        .with(authAs(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "inviteCode": "INVALID1" }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invite_code_not_found"));
    }

    @Test
    void joinChallenge_challengeFull_returns400() throws Exception {
        when(friendChallengeService.joinChallenge(any(), any()))
                .thenThrow(new ChallengeFullException());

        mockMvc.perform(post("/api/v1/friend-challenges/join")
                        .with(authAs(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "inviteCode": "FULL1234" }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("challenge_full"));
    }

    @Test
    void joinChallenge_alreadyParticipant_returns400() throws Exception {
        when(friendChallengeService.joinChallenge(any(), any()))
                .thenThrow(new AlreadyParticipantException());

        mockMvc.perform(post("/api/v1/friend-challenges/join")
                        .with(authAs(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "inviteCode": "ABCD1234" }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("already_participant"));
    }

    // --- GET /api/v1/friend-challenges ---

    @Test
    void listChallenges_authenticated_returns200() throws Exception {
        when(friendChallengeService.listChallenges(userId)).thenReturn(List.of(buildChallenge()));
        when(checkInService.getRanking(userId, challengeId)).thenReturn(new RankingView(
                ChallengeType.DISTANCE,
                List.of(new RankingEntry(2, userId, "Joao", 10.0, 1))
        ));

        mockMvc.perform(get("/api/v1/friend-challenges")
                        .with(authAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].start_date").exists())
                .andExpect(jsonPath("$[0].participant_count").value(1))
                .andExpect(jsonPath("$[0].user_role").value("CREATOR"))
                .andExpect(jsonPath("$[0].user_rank_position").value(2));
    }

    @Test
    void listChallenges_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/friend-challenges"))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/v1/friend-challenges/{id} ---

    @Test
    void getChallengeDetail_participant_returns200() throws Exception {
        FriendChallengeParticipant participant = new FriendChallengeParticipant(
                userId, "Joao", ParticipantRole.CREATOR, OffsetDateTime.now()
        );
        FriendChallenge detailed = new FriendChallenge(
                challengeId, userId, "Corrida de Maio", null,
                ChallengeType.DISTANCE, BigDecimal.valueOf(50.0),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(30),
                "ABCD1234", "ACTIVE", 1, 5, ParticipantRole.CREATOR,
                OffsetDateTime.now(), List.of(participant)
        );
        when(friendChallengeService.getChallengeDetail(userId, challengeId))
                .thenReturn(new FriendChallengeDetailView(detailed, List.of(participant)));
        when(checkInService.getRanking(userId, challengeId)).thenReturn(new RankingView(
                ChallengeType.DISTANCE,
                List.of(new RankingEntry(1, userId, "Joao", 12.5, 2))
        ));
        when(checkInService.listCheckIns(userId, challengeId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/friend-challenges/{id}", challengeId)
                        .with(authAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invite_code").value("ABCD1234"))
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.participants[0].name").value("Joao"))
                .andExpect(jsonPath("$.ranking[0].position").value(1))
                .andExpect(jsonPath("$.check_ins").isArray())
                .andExpect(jsonPath("$.permissions.can_check_in").value(false))
                .andExpect(jsonPath("$.permissions.can_delete").value(true));
    }

    @Test
    void getChallengeDetail_nonParticipant_returns403() throws Exception {
        when(friendChallengeService.getChallengeDetail(userId, challengeId))
                .thenThrow(new FriendChallengeAccessDeniedException());

        mockMvc.perform(get("/api/v1/friend-challenges/{id}", challengeId)
                        .with(authAs(userId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("access_denied"));
    }

    @Test
    void getChallengeDetail_notFound_returns404() throws Exception {
        when(friendChallengeService.getChallengeDetail(userId, challengeId))
                .thenThrow(new FriendChallengeNotFoundException(challengeId));

        mockMvc.perform(get("/api/v1/friend-challenges/{id}", challengeId)
                        .with(authAs(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void leaveChallenge_member_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/friend-challenges/{id}/participants/me", challengeId)
                        .with(authAs(userId)))
                .andExpect(status().isNoContent());

        verify(friendChallengeService).leaveChallenge(userId, challengeId);
    }

    @Test
    void leaveChallenge_unauthenticated_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/friend-challenges/{id}/participants/me", challengeId))
                .andExpect(status().isForbidden());
    }

    @Test
    void leaveChallenge_inactiveChallenge_returns400() throws Exception {
        doThrow(new ChallengeNotActiveException())
                .when(friendChallengeService).leaveChallenge(any(), eq(challengeId));

        mockMvc.perform(delete("/api/v1/friend-challenges/{id}/participants/me", challengeId)
                        .with(authAs(userId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("challenge_not_active"));
    }

    @Test
    void deleteChallenge_creator_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/friend-challenges/{id}", challengeId)
                        .with(authAs(userId)))
                .andExpect(status().isNoContent());

        verify(friendChallengeService).deleteChallenge(userId, challengeId);
    }

    @Test
    void deleteChallenge_notCreator_returns403() throws Exception {
        doThrow(new FriendChallengeAccessDeniedException())
                .when(friendChallengeService).deleteChallenge(any(), eq(challengeId));

        mockMvc.perform(delete("/api/v1/friend-challenges/{id}", challengeId)
                        .with(authAs(userId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("access_denied"));
    }

    @Test
    void deleteChallenge_alreadyDeleted_returns404() throws Exception {
        doThrow(new FriendChallengeNotFoundException(challengeId))
                .when(friendChallengeService).deleteChallenge(any(), eq(challengeId));

        mockMvc.perform(delete("/api/v1/friend-challenges/{id}", challengeId)
                        .with(authAs(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }
}
