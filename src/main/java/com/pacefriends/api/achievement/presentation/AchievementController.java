package com.pacefriends.api.achievement.presentation;

import com.pacefriends.api.achievement.application.GetUserAchievementsUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Achievements")
@RestController
@RequestMapping("/api/v1/achievements")
public class AchievementController {

    private final GetUserAchievementsUseCase getUserAchievementsUseCase;

    public AchievementController(GetUserAchievementsUseCase getUserAchievementsUseCase) {
        this.getUserAchievementsUseCase = getUserAchievementsUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<List<AchievementResponse>> getMyAchievements(
            @AuthenticationPrincipal UUID userId) {
        List<AchievementResponse> response = getUserAchievementsUseCase.execute(userId)
                .stream()
                .map(AchievementResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
