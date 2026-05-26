package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.friendchallenge.application.FriendChallengeDetailView;
import com.pacefriends.api.friendchallenge.application.FriendChallengeService;
import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friend-challenges")
public class FriendChallengeController {

    private final FriendChallengeService friendChallengeService;

    public FriendChallengeController(FriendChallengeService friendChallengeService) {
        this.friendChallengeService = friendChallengeService;
    }

    @PostMapping
    public ResponseEntity<FriendChallengeResponse> createChallenge(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CreateFriendChallengeRequest request) {

        FriendChallenge challenge = friendChallengeService.createChallenge(
                userId,
                request.title(),
                request.description(),
                request.challengeType(),
                request.goalValue(),
                request.startDate(),
                request.endDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(FriendChallengeResponse.from(challenge));
    }

    @PostMapping("/join")
    public ResponseEntity<FriendChallengeResponse> joinChallenge(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody JoinChallengeRequest request) {

        FriendChallenge challenge = friendChallengeService.joinChallenge(userId, request.inviteCode());
        return ResponseEntity.ok(FriendChallengeResponse.from(challenge));
    }

    @GetMapping
    public ResponseEntity<List<FriendChallengeResponse>> listChallenges(
            @AuthenticationPrincipal UUID userId) {

        List<FriendChallenge> challenges = friendChallengeService.listChallenges(userId);
        List<FriendChallengeResponse> response = challenges.stream()
                .map(FriendChallengeResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FriendChallengeDetailResponse> getChallengeDetail(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {

        FriendChallengeDetailView detail = friendChallengeService.getChallengeDetail(userId, id);
        return ResponseEntity.ok(FriendChallengeDetailResponse.from(detail));
    }

    @DeleteMapping("/{id}/participants/me")
    public ResponseEntity<Void> leaveChallenge(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        friendChallengeService.leaveChallenge(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChallenge(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        friendChallengeService.deleteChallenge(userId, id);
        return ResponseEntity.noContent().build();
    }
}
