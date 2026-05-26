package com.pacefriends.api.trail.presentation;

import com.pacefriends.api.auth.JwtUtil;
import com.pacefriends.api.config.JacksonConfig;
import com.pacefriends.api.config.SecurityConfig;
import com.pacefriends.api.trail.application.TrailService;
import com.pacefriends.api.trail.domain.*;
import com.pacefriends.api.trail.domain.exception.LevelUpNotAllowedException;
import com.pacefriends.api.trail.domain.exception.TrailAccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrailController.class)
@Import({SecurityConfig.class, JacksonConfig.class, JwtUtil.class})
@ActiveProfiles("test")
class TrailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrailService trailService;

    @MockBean
    private com.pacefriends.api.user.UserRepository userRepository;

    private final UUID userId = UUID.randomUUID();

    // --- GET /api/v1/users/{userId}/training-path ---

    @Test
    void getTrainingPath_authenticated_returns200() throws Exception {
        TrainingPathData data = buildSampleData(userId, 1, false);
        when(trailService.getTrainingPath(eq(userId), eq(userId))).thenReturn(data);

        mockMvc.perform(get("/api/v1/users/{userId}/training-path", userId)
                        .with(authAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.currentLevel").value(1))
                .andExpect(jsonPath("$.currentLevelName").value("Iniciante"))
                .andExpect(jsonPath("$.path.totalItems").value(10))
                .andExpect(jsonPath("$.path.items").isArray())
                .andExpect(jsonPath("$.path.items[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.path.items[0].xpReward").value(10))
                .andExpect(jsonPath("$.path.items[1].status").value("LOCKED"))
                .andExpect(jsonPath("$.canLevelUp").value(false));
    }

    @Test
    void getTrainingPath_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}/training-path", userId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTrainingPath_differentUser_returns403() throws Exception {
        when(trailService.getTrainingPath(eq(userId), eq(UUID.fromString(userId.toString()))))
                .thenThrow(new TrailAccessDeniedException());

        UUID otherUserId = UUID.randomUUID();
        when(trailService.getTrainingPath(eq(userId), eq(otherUserId)))
                .thenThrow(new TrailAccessDeniedException());

        mockMvc.perform(get("/api/v1/users/{userId}/training-path", otherUserId)
                        .with(authAs(userId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("access_denied"));
    }

    // --- POST /api/v1/users/{userId}/training-path/level-up ---

    @Test
    void levelUp_conditionsMet_returns200() throws Exception {
        LevelUpResult result = new LevelUpResult(1, 2, "Explorador");
        when(trailService.levelUp(eq(userId), eq(userId))).thenReturn(result);

        mockMvc.perform(post("/api/v1/users/{userId}/training-path/level-up", userId)
                        .with(authAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.previousLevel").value(1))
                .andExpect(jsonPath("$.newLevel").value(2))
                .andExpect(jsonPath("$.newLevelName").value("Explorador"));
    }

    @Test
    void levelUp_conditionsNotMet_returns400() throws Exception {
        when(trailService.levelUp(eq(userId), eq(userId)))
                .thenThrow(new LevelUpNotAllowedException("A trilha atual nao foi completada."));

        mockMvc.perform(post("/api/v1/users/{userId}/training-path/level-up", userId)
                        .with(authAs(userId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("level_up_not_allowed"));
    }

    @Test
    void levelUp_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/users/{userId}/training-path/level-up", userId))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---

    private RequestPostProcessor authAs(UUID id) {
        return authentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        id, null, Collections.emptyList()));
    }

    private TrainingPathData buildSampleData(UUID uid, int level, boolean canLevelUp) {
        List<TrainingPathItem> items = List.of(
                new TrainingPathItem(1, "Complete 1 atividade(s)", 10, ItemStatus.IN_PROGRESS, null),
                new TrainingPathItem(2, "Complete 3 atividades", 20, ItemStatus.LOCKED, null)
        );
        TrainingPath path = new TrainingPath(level, 0, 10, null, false, items);
        NextLevelRequirements reqs = new NextLevelRequirements(false, 4, 0, level * 650, 0);
        return new TrainingPathData(uid, level, "Iniciante", path, canLevelUp, reqs);
    }
}
