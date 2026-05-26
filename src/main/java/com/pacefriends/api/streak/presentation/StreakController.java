package com.pacefriends.api.streak.presentation;

import com.pacefriends.api.streak.application.StreakQueryService;
import com.pacefriends.api.streak.application.StreakView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/streak")
public class StreakController {

    private final StreakQueryService streakQueryService;

    public StreakController(StreakQueryService streakQueryService) {
        this.streakQueryService = streakQueryService;
    }

    @GetMapping
    public ResponseEntity<StreakResponse> getStreak(@AuthenticationPrincipal UUID userId) {
        StreakView view = streakQueryService.getStreakView(userId);
        return ResponseEntity.ok(StreakResponse.from(view));
    }
}
