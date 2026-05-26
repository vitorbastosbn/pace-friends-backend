package com.pacefriends.api.profile.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacefriends.api.auth.JwtUtil;
import com.pacefriends.api.config.JacksonConfig;
import com.pacefriends.api.config.SecurityConfig;
import com.pacefriends.api.profile.application.ProfileService;
import com.pacefriends.api.profile.domain.ProfileData;
import com.pacefriends.api.profile.domain.ProfileStats;
import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.domain.exception.ProfileAccessDeniedException;
import com.pacefriends.api.profile.domain.exception.UserSettingsNotFoundException;
import com.pacefriends.api.streak.application.UpdateWeeklyFrequencyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
@Import({SecurityConfig.class, JacksonConfig.class, JwtUtil.class})
@ActiveProfiles("test")
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private UpdateWeeklyFrequencyService updateWeeklyFrequencyService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void getProfile_authenticated_returns200() throws Exception {
        ProfileData data = buildProfileData(userId);
        when(profileService.getUserProfile(eq(userId), eq(userId))).thenReturn(data);

        mockMvc.perform(get("/api/v1/users/{userId}/profile", userId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        userId, null, java.util.Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.objective").value("LOSE_WEIGHT"))
                .andExpect(jsonPath("$.weeklyFrequency").value("THREE"))
                .andExpect(jsonPath("$.stats.totalXp").value(0));
    }

    @Test
    void getProfile_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}/profile", userId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfile_accessDenied_returns403() throws Exception {
        when(profileService.getUserProfile(any(), eq(userId)))
                .thenThrow(new ProfileAccessDeniedException());

        mockMvc.perform(get("/api/v1/users/{userId}/profile", userId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        UUID.randomUUID(), null, java.util.Collections.emptyList()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("access_denied"));
    }

    @Test
    void getProfile_settingsNotFound_returns404() throws Exception {
        when(profileService.getUserProfile(eq(userId), eq(userId)))
                .thenThrow(new UserSettingsNotFoundException(userId));

        mockMvc.perform(get("/api/v1/users/{userId}/profile", userId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        userId, null, java.util.Collections.emptyList()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void updateProfile_validRequest_returns200() throws Exception {
        ProfileData data = buildProfileData(userId);
        when(profileService.updateProfile(eq(userId), eq(userId),
                eq(UserObjective.LOSE_WEIGHT), eq(WeeklyFrequency.THREE)))
                .thenReturn(data);

        mockMvc.perform(put("/api/v1/users/{userId}/profile", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "objective": "LOSE_WEIGHT", "weeklyFrequency": "THREE" }
                                """)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        userId, null, java.util.Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objective").value("LOSE_WEIGHT"))
                .andExpect(jsonPath("$.weeklyFrequency").value("THREE"));
    }

    @Test
    void updateProfile_invalidObjective_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/users/{userId}/profile", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "objective": "INVALID_VALUE", "weeklyFrequency": "THREE" }
                                """)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        userId, null, java.util.Collections.emptyList()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    @Test
    void updateProfile_missingObjective_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/users/{userId}/profile", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "weeklyFrequency": "THREE" }
                                """)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        userId, null, java.util.Collections.emptyList()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"));
    }

    @Test
    void updateProfile_accessDenied_returns403() throws Exception {
        when(profileService.updateProfile(any(), eq(userId), any(), any()))
                .thenThrow(new ProfileAccessDeniedException());

        mockMvc.perform(put("/api/v1/users/{userId}/profile", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "objective": "LOSE_WEIGHT", "weeklyFrequency": "THREE" }
                                """)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        UUID.randomUUID(), null, java.util.Collections.emptyList()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateFrequency_validRequest_returnsSundayEffectiveResponse() throws Exception {
        when(updateWeeklyFrequencyService.update(eq(userId), eq(5)))
                .thenReturn(new UpdateWeeklyFrequencyService.FrequencyUpdate(
                        5, LocalDate.of(2026, 5, 31)));

        mockMvc.perform(put("/api/v1/users/me/frequency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "weeklyFrequency": 5 }
                                """)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        userId, null, java.util.Collections.emptyList()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyFrequency").value(5))
                .andExpect(jsonPath("$.effectiveFrom").value("2026-05-31"));
    }

    @Test
    void updateFrequency_outsideAllowedRange_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/users/me/frequency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "weeklyFrequency": 8 }
                                """)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                        userId, null, java.util.Collections.emptyList()))))
                .andExpect(status().isBadRequest());
    }

    private ProfileData buildProfileData(UUID userId) {
        return ProfileData.builder()
                .userId(userId)
                .name("Test User")
                .email("user@example.com")
                .photoUrl("https://photo.url")
                .objective(UserObjective.LOSE_WEIGHT)
                .weeklyFrequency(WeeklyFrequency.THREE)
                .effectiveFrom(LocalDate.now())
                .stats(ProfileStats.empty())
                .build();
    }
}
