package com.pacefriends.api.trail.presentation;

import com.pacefriends.api.trail.application.TrailService;
import com.pacefriends.api.trail.domain.LevelUpResult;
import com.pacefriends.api.trail.domain.TrainingPathData;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Training Path")
@RestController
@RequestMapping("/api/v1/users/{userId}/training-path")
public class TrailController {

    private final TrailService trailService;

    public TrailController(TrailService trailService) {
        this.trailService = trailService;
    }

    @GetMapping
    public ResponseEntity<TrainingPathResponse> getTrainingPath(
            @AuthenticationPrincipal UUID requestingUserId,
            @PathVariable UUID userId) {

        TrainingPathData data = trailService.getTrainingPath(requestingUserId, userId);
        return ResponseEntity.ok(TrainingPathResponse.from(data));
    }

    @PostMapping("/level-up")
    public ResponseEntity<LevelUpResponse> levelUp(
            @AuthenticationPrincipal UUID requestingUserId,
            @PathVariable UUID userId) {

        LevelUpResult result = trailService.levelUp(requestingUserId, userId);
        return ResponseEntity.ok(LevelUpResponse.from(result));
    }
}
