package com.pacefriends.api.home.presentation;

import com.pacefriends.api.auth.JwtUtil;
import com.pacefriends.api.config.JacksonConfig;
import com.pacefriends.api.config.SecurityConfig;
import com.pacefriends.api.home.application.HomeSummaryService;
import com.pacefriends.api.home.domain.HomeSummary;
import com.pacefriends.api.profile.domain.exception.ProfileAccessDeniedException;
import com.pacefriends.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeSummaryController.class)
@Import({SecurityConfig.class, JacksonConfig.class, JwtUtil.class})
@ActiveProfiles("test")
class HomeSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HomeSummaryService homeSummaryService;

    @MockBean
    private UserRepository userRepository;

    private final UUID userId = UUID.randomUUID();

    @Test
    void getHomeSummary_authenticated_returnsResponseContract() throws Exception {
        when(homeSummaryService.getHomeSummary(eq(userId), eq(userId)))
                .thenReturn(new HomeSummary(
                        new HomeSummary.Streak(5, "days"),
                        new HomeSummary.Xp(1250),
                        new HomeSummary.Level(5, 1500),
                        new HomeSummary.WeeklyFrequency(2, 4),
                        new HomeSummary.TrainingPath("Iniciante", 40, true)));

        mockMvc.perform(get("/api/v1/users/{userId}/home-summary", userId)
                        .with(authAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.streak.current").value(5))
                .andExpect(jsonPath("$.xp.total").value(1250))
                .andExpect(jsonPath("$.level.xp_for_next_level").value(1500))
                .andExpect(jsonPath("$.weekly_frequency.days_trained").value(2))
                .andExpect(jsonPath("$.training_path.progress_percent").value(40))
                .andExpect(jsonPath("$.training_path.available").value(true));
    }

    @Test
    void getHomeSummary_differentUser_returnsForbidden() throws Exception {
        UUID anotherUserId = UUID.randomUUID();
        when(homeSummaryService.getHomeSummary(eq(userId), eq(anotherUserId)))
                .thenThrow(new ProfileAccessDeniedException());

        mockMvc.perform(get("/api/v1/users/{userId}/home-summary", anotherUserId)
                        .with(authAs(userId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("access_denied"));
    }

    @Test
    void getHomeSummary_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}/home-summary", userId))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor authAs(UUID id) {
        return authentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        id, null, Collections.emptyList()));
    }
}
