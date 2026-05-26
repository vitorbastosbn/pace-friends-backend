package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.auth.JwtUtil;
import com.pacefriends.api.config.JacksonConfig;
import com.pacefriends.api.config.SecurityConfig;
import com.pacefriends.api.friendchallenge.application.CheckInService;
import com.pacefriends.api.friendchallenge.domain.FriendChallengeCheckIn;
import com.pacefriends.api.friendchallenge.domain.exception.ChallengeNotInAuditException;
import com.pacefriends.api.friendchallenge.domain.exception.FriendChallengeAccessDeniedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CheckInController.class)
@Import({SecurityConfig.class, JacksonConfig.class, JwtUtil.class})
@ActiveProfiles("test")
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckInService checkInService;

    @MockBean
    private com.pacefriends.api.user.UserRepository userRepository;

    private final UUID creatorId = UUID.randomUUID();
    private final UUID challengeId = UUID.randomUUID();
    private final UUID checkInId = UUID.randomUUID();

    private RequestPostProcessor authAs(UUID id) {
        return authentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        id, null, Collections.emptyList()));
    }

    @Test
    void rejectCheckIn_creatorDuringAudit_returnsRejectedCheckIn() throws Exception {
        FriendChallengeCheckIn rejected = new FriendChallengeCheckIn(
                checkInId, challengeId, creatorId, 5.0, 1800, 360,
                LocalDate.now().minusDays(1), null, "REMOVED_BY_CREATOR", OffsetDateTime.now()
        );
        when(checkInService.rejectCheckIn(creatorId, challengeId, checkInId)).thenReturn(rejected);

        mockMvc.perform(patch("/api/v1/friend-challenges/{challengeId}/check-ins/{checkInId}/reject",
                        challengeId, checkInId)
                        .with(authAs(creatorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(checkInId.toString()))
                .andExpect(jsonPath("$.status").value("REMOVED_BY_CREATOR"));
    }

    @Test
    void rejectCheckIn_nonCreator_returns403() throws Exception {
        when(checkInService.rejectCheckIn(creatorId, challengeId, checkInId))
                .thenThrow(new FriendChallengeAccessDeniedException());

        mockMvc.perform(patch("/api/v1/friend-challenges/{challengeId}/check-ins/{checkInId}/reject",
                        challengeId, checkInId)
                        .with(authAs(creatorId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("access_denied"));
    }

    @Test
    void rejectCheckIn_outsideAudit_returns422() throws Exception {
        when(checkInService.rejectCheckIn(creatorId, challengeId, checkInId))
                .thenThrow(new ChallengeNotInAuditException());

        mockMvc.perform(patch("/api/v1/friend-challenges/{challengeId}/check-ins/{checkInId}/reject",
                        challengeId, checkInId)
                        .with(authAs(creatorId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("challenge_not_in_audit"));
    }
}
