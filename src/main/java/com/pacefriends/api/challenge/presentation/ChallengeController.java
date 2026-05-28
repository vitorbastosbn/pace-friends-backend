package com.pacefriends.api.challenge.presentation;

import com.pacefriends.api.challenge.application.ChallengeDetailView;
import com.pacefriends.api.challenge.application.ChallengeService;
import com.pacefriends.api.challenge.domain.Activity;
import com.pacefriends.api.challenge.domain.Challenge;
import com.pacefriends.api.challenge.domain.ChallengeProgress;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Challenges")
@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @PostMapping
    public ResponseEntity<ChallengeResponse> createChallenge(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateChallengeRequest request) {

        Challenge challenge = challengeService.createChallenge(
                userId,
                request.title(),
                request.goalDistanceKm(),
                request.deadline()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ChallengeResponse.from(challenge));
    }

    @GetMapping
    public ResponseEntity<List<ChallengeProgressResponse>> listChallenges(
            @AuthenticationPrincipal UUID userId) {

        List<ChallengeProgress> progresses = challengeService.listChallenges(userId);
        List<ChallengeProgressResponse> response = progresses.stream()
                .map(ChallengeProgressResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<IndividualChallengeResponse> getMyChallenge(
            @AuthenticationPrincipal UUID userId) {
        return challengeService.getMyActiveChallenge(userId)
                .map(IndividualChallengeResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChallengeProgressResponse> getChallengeDetail(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {

        ChallengeDetailView detail = challengeService.getChallengeWithActivities(userId, id);
        List<ActivityResponse> activityResponses = detail.activities().stream()
                .map(ActivityResponse::from)
                .toList();
        return ResponseEntity.ok(ChallengeProgressResponse.from(detail.progress(), activityResponses));
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody RegisterActivityRequest request) {

        Activity activity = challengeService.registerActivity(
                userId,
                id,
                request.distanceKm(),
                request.durationSeconds(),
                request.activityDate(),
                request.notes()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ActivityResponse.from(activity));
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityResponse>> listActivities(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {

        List<Activity> activities = challengeService.listActivities(userId, id);
        List<ActivityResponse> response = activities.stream()
                .map(ActivityResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
