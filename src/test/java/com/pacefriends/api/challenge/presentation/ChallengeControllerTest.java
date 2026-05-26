package com.pacefriends.api.challenge.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacefriends.api.auth.JwtUtil;
import com.pacefriends.api.challenge.application.ChallengeDetailView;
import com.pacefriends.api.challenge.application.ChallengeService;
import com.pacefriends.api.challenge.domain.Activity;
import com.pacefriends.api.challenge.domain.Challenge;
import com.pacefriends.api.challenge.domain.ChallengeProgress;
import com.pacefriends.api.challenge.domain.ChallengeStatus;
import com.pacefriends.api.challenge.domain.exception.ChallengeAccessDeniedException;
import com.pacefriends.api.challenge.domain.exception.ChallengeNotFoundException;
import com.pacefriends.api.config.JacksonConfig;
import com.pacefriends.api.config.SecurityConfig;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChallengeController.class)
@Import({SecurityConfig.class, JacksonConfig.class, JwtUtil.class})
@ActiveProfiles("test")
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChallengeService challengeService;

    private final UUID userId = UUID.randomUUID();
    private final UUID challengeId = UUID.randomUUID();

    // --- POST /api/v1/challenges ---

    @Test
    void createChallenge_validRequest_returns201() throws Exception {
        Challenge challenge = buildChallenge(challengeId, userId, ChallengeStatus.ACTIVE, new BigDecimal("50.00"));
        when(challengeService.createChallenge(eq(userId), any(), any(), any())).thenReturn(challenge);

        mockMvc.perform(post("/api/v1/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Run 50km",
                                    "goalDistanceKm": 50.0,
                                    "deadline": "%s"
                                }
                                """.formatted(LocalDate.now().plusDays(30)))
                        .with(authAs(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(challengeId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createChallenge_missingTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "goalDistanceKm": 50.0,
                                    "deadline": "%s"
                                }
                                """.formatted(LocalDate.now().plusDays(30)))
                        .with(authAs(userId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    @Test
    void createChallenge_titleTooShort_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "AB",
                                    "goalDistanceKm": 10.0,
                                    "deadline": "%s"
                                }
                                """.formatted(LocalDate.now().plusDays(30)))
                        .with(authAs(userId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    @Test
    void createChallenge_deadlineInPast_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Old run",
                                    "goalDistanceKm": 10.0,
                                    "deadline": "%s"
                                }
                                """.formatted(LocalDate.now().minusDays(1)))
                        .with(authAs(userId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    @Test
    void createChallenge_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "X", "goalDistanceKm": 10.0, "deadline": "%s"}
                                """.formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/v1/challenges ---

    @Test
    void listChallenges_authenticated_returns200WithList() throws Exception {
        ChallengeProgress progress = buildProgress(challengeId, userId, new BigDecimal("10.00"), new BigDecimal("20.00"));
        when(challengeService.listChallenges(userId)).thenReturn(List.of(progress));

        mockMvc.perform(get("/api/v1/challenges")
                        .with(authAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(challengeId.toString()))
                .andExpect(jsonPath("$[0].progressKm").value(10.00))
                .andExpect(jsonPath("$[0].progressPct").value(20.00));
    }

    @Test
    void listChallenges_emptyList_returns200WithEmptyArray() throws Exception {
        when(challengeService.listChallenges(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/challenges")
                        .with(authAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- GET /api/v1/challenges/{id} ---

    @Test
    void getChallengeDetail_ownChallenge_returns200WithActivities() throws Exception {
        ChallengeProgress progress = buildProgress(challengeId, userId, new BigDecimal("25.00"), new BigDecimal("50.00"));
        Activity activity = buildActivity(UUID.randomUUID(), challengeId, userId);
        when(challengeService.getChallengeWithActivities(userId, challengeId))
                .thenReturn(new ChallengeDetailView(progress, List.of(activity)));

        mockMvc.perform(get("/api/v1/challenges/{id}", challengeId)
                        .with(authAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(challengeId.toString()))
                .andExpect(jsonPath("$.progressPct").value(50.00))
                .andExpect(jsonPath("$.activities").isArray())
                .andExpect(jsonPath("$.activities[0].distanceKm").value(10.0));
    }

    @Test
    void getChallengeDetail_notFound_returns404() throws Exception {
        when(challengeService.getChallengeWithActivities(userId, challengeId))
                .thenThrow(new ChallengeNotFoundException(challengeId));

        mockMvc.perform(get("/api/v1/challenges/{id}", challengeId)
                        .with(authAs(userId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void getChallengeDetail_differentUser_returns403() throws Exception {
        when(challengeService.getChallengeWithActivities(any(), eq(challengeId)))
                .thenThrow(new ChallengeAccessDeniedException());

        mockMvc.perform(get("/api/v1/challenges/{id}", challengeId)
                        .with(authAs(UUID.randomUUID())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("access_denied"));
    }

    // --- POST /api/v1/challenges/{id}/activities ---

    @Test
    void registerActivity_validRequest_returns201() throws Exception {
        Activity activity = buildActivity(UUID.randomUUID(), challengeId, userId);
        when(challengeService.registerActivity(eq(userId), eq(challengeId), any(), any(), any(), any()))
                .thenReturn(activity);

        mockMvc.perform(post("/api/v1/challenges/{id}/activities", challengeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "distanceKm": 10.0,
                                    "durationSeconds": 3600,
                                    "activityDate": "%s"
                                }
                                """.formatted(LocalDate.now()))
                        .with(authAs(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.distanceKm").value(10.0))
                .andExpect(jsonPath("$.paceSecondsPerKm").value(360.00));
    }

    @Test
    void registerActivity_futureDateActivity_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/challenges/{id}/activities", challengeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "distanceKm": 10.0,
                                    "durationSeconds": 3600,
                                    "activityDate": "%s"
                                }
                                """.formatted(LocalDate.now().plusDays(1)))
                        .with(authAs(userId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    @Test
    void registerActivity_missingDistanceKm_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/challenges/{id}/activities", challengeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "durationSeconds": 3600,
                                    "activityDate": "%s"
                                }
                                """.formatted(LocalDate.now()))
                        .with(authAs(userId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    @Test
    void registerActivity_challengeNotFound_returns404() throws Exception {
        when(challengeService.registerActivity(any(), eq(challengeId), any(), any(), any(), any()))
                .thenThrow(new ChallengeNotFoundException(challengeId));

        mockMvc.perform(post("/api/v1/challenges/{id}/activities", challengeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "distanceKm": 10.0,
                                    "durationSeconds": 3600,
                                    "activityDate": "%s"
                                }
                                """.formatted(LocalDate.now()))
                        .with(authAs(userId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerActivity_differentUser_returns403() throws Exception {
        when(challengeService.registerActivity(any(), eq(challengeId), any(), any(), any(), any()))
                .thenThrow(new ChallengeAccessDeniedException());

        mockMvc.perform(post("/api/v1/challenges/{id}/activities", challengeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "distanceKm": 10.0,
                                    "durationSeconds": 3600,
                                    "activityDate": "%s"
                                }
                                """.formatted(LocalDate.now()))
                        .with(authAs(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    // --- GET /api/v1/challenges/{id}/activities ---

    @Test
    void listActivities_ownChallenge_returns200() throws Exception {
        Activity activity = buildActivity(UUID.randomUUID(), challengeId, userId);
        when(challengeService.listActivities(userId, challengeId)).thenReturn(List.of(activity));

        mockMvc.perform(get("/api/v1/challenges/{id}/activities", challengeId)
                        .with(authAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].distanceKm").value(10.0));
    }

    @Test
    void listActivities_differentUser_returns403() throws Exception {
        when(challengeService.listActivities(any(), eq(challengeId)))
                .thenThrow(new ChallengeAccessDeniedException());

        mockMvc.perform(get("/api/v1/challenges/{id}/activities", challengeId)
                        .with(authAs(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void listActivities_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/challenges/{id}/activities", challengeId))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---

    private RequestPostProcessor authAs(UUID id) {
        return authentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        id, null, Collections.emptyList()));
    }

    private Challenge buildChallenge(UUID id, UUID owner, ChallengeStatus status, BigDecimal goal) {
        return Challenge.builder()
                .id(id)
                .userId(owner)
                .title("Test Challenge")
                .goalDistanceKm(goal)
                .deadline(LocalDate.now().plusDays(30))
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ChallengeProgress buildProgress(UUID id, UUID owner, BigDecimal progressKm, BigDecimal progressPct) {
        Challenge challenge = buildChallenge(id, owner, ChallengeStatus.ACTIVE, new BigDecimal("50.00"));
        return new ChallengeProgress(challenge, progressKm, progressPct);
    }

    private Activity buildActivity(UUID id, UUID challengeId, UUID userId) {
        return Activity.builder()
                .id(id)
                .challengeId(challengeId)
                .userId(userId)
                .distanceKm(new BigDecimal("10.000"))
                .durationSeconds(3600)
                .paceSecondsPerKm(new BigDecimal("360.00"))
                .activityDate(LocalDate.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
