package com.pacefriends.api.streak.presentation;

import com.pacefriends.api.auth.JwtUtil;
import com.pacefriends.api.config.JacksonConfig;
import com.pacefriends.api.config.SecurityConfig;
import com.pacefriends.api.streak.application.StreakQueryService;
import com.pacefriends.api.streak.application.StreakView;
import com.pacefriends.api.streak.domain.XpCalculation;
import com.pacefriends.api.streak.domain.StreakResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StreakController.class)
@Import({SecurityConfig.class, JacksonConfig.class, JwtUtil.class})
@ActiveProfiles("test")
class StreakControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StreakQueryService streakQueryService;

    @MockBean
    private com.pacefriends.api.user.UserRepository userRepository;

    @Test
    void getStreak_authenticated_returnsProgressAndXpPayload() throws Exception {
        UUID userId = UUID.randomUUID();
        when(streakQueryService.getStreakView(userId)).thenReturn(new StreakView(
                5, 4, 2, 2, XpCalculation.forTargetFrequency(4), StreakResult.MAINTAINED));

        mockMvc.perform(get("/api/v1/streak")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(
                                        userId, null, Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreak").value(5))
                .andExpect(jsonPath("$.daysCompletedThisWeek").value(2))
                .andExpect(jsonPath("$.xpProgress.potentialXp").value(40))
                .andExpect(jsonPath("$.xpProgress.potentialXpIfBroken").value(-40))
                .andExpect(jsonPath("$.lastResult").value("MAINTAINED"));
    }

    @Test
    void getStreak_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/streak"))
                .andExpect(status().isForbidden());
    }
}
