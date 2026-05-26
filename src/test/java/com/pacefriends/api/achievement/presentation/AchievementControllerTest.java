package com.pacefriends.api.achievement.presentation;

import com.pacefriends.api.achievement.application.AchievementView;
import com.pacefriends.api.achievement.application.GetUserAchievementsUseCase;
import com.pacefriends.api.auth.JwtUtil;
import com.pacefriends.api.config.JacksonConfig;
import com.pacefriends.api.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AchievementController.class)
@Import({SecurityConfig.class, JacksonConfig.class, JwtUtil.class})
@ActiveProfiles("test")
class AchievementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetUserAchievementsUseCase getUserAchievementsUseCase;

    @MockBean
    private com.pacefriends.api.user.UserRepository userRepository;

    private final UUID userId = UUID.randomUUID();

    @Test
    void getMyAchievements_authenticated_returnsFullList() throws Exception {
        AchievementView unlocked = new AchievementView(
                UUID.randomUUID(), "primeiro-passo", "Primeiro Passo", "Registrou a primeira atividade",
                "footsteps", true, OffsetDateTime.now(), null, null);
        AchievementView locked = new AchievementView(
                UUID.randomUUID(), "corredor-iniciante", "Corredor Iniciante", "Registre 5 atividades",
                "runner", false, null, 3, 5);

        when(getUserAchievementsUseCase.execute(any())).thenReturn(List.of(unlocked, locked));

        mockMvc.perform(get("/api/v1/achievements/me").with(authAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("primeiro-passo"))
                .andExpect(jsonPath("$[0].unlocked").value(true))
                .andExpect(jsonPath("$[0].progress").isEmpty())
                .andExpect(jsonPath("$[1].slug").value("corredor-iniciante"))
                .andExpect(jsonPath("$[1].unlocked").value(false))
                .andExpect(jsonPath("$[1].progress.current").value(3))
                .andExpect(jsonPath("$[1].progress.total").value(5));
    }

    @Test
    void getMyAchievements_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/achievements/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyAchievements_progressNullForBinaryAchievement_progressFieldAbsent() throws Exception {
        AchievementView binary = new AchievementView(
                UUID.randomUUID(), "primeira-vitoria", "Primeira Vitoria", "Venceu o primeiro desafio",
                "trophy", false, null, null, null);

        when(getUserAchievementsUseCase.execute(any())).thenReturn(List.of(binary));

        mockMvc.perform(get("/api/v1/achievements/me").with(authAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].progress").isEmpty());
    }

    private RequestPostProcessor authAs(UUID id) {
        return authentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        id, null, Collections.emptyList()));
    }
}
