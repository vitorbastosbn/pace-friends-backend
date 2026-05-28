package com.pacefriends.api.friendchallenge.presentation;

import com.pacefriends.api.friendchallenge.application.FriendChallengeDetailView;
import com.pacefriends.api.friendchallenge.application.FriendChallengeService;
import com.pacefriends.api.friendchallenge.application.CheckInService;
import com.pacefriends.api.friendchallenge.domain.FriendChallenge;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Friend Challenges")
@RestController
@RequestMapping("/api/v1/friend-challenges")
public class FriendChallengeController {

    private final FriendChallengeService friendChallengeService;
    private final CheckInService checkInService;

    public FriendChallengeController(FriendChallengeService friendChallengeService,
                                     CheckInService checkInService) {
        this.friendChallengeService = friendChallengeService;
        this.checkInService = checkInService;
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

    @GetMapping("/history")
    public ResponseEntity<FriendChallengeHistoryPageResponse> listHistory(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        FriendChallengeService.HistoryPage historyPage = friendChallengeService.listHistory(userId, page, size);
        FriendChallengeHistoryPageResponse response = FriendChallengeHistoryPageResponse.from(
                historyPage, page, size,
                challenge -> checkInService.getRanking(userId, challenge.id()).entries().stream()
                        .filter(entry -> userId.equals(entry.userId()))
                        .map(entry -> entry.position())
                        .findFirst()
                        .orElse(null)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FriendChallengeListItemResponse>> listChallenges(
            @AuthenticationPrincipal UUID userId) {

        List<FriendChallenge> challenges = friendChallengeService.listChallenges(userId);
        List<FriendChallengeListItemResponse> response = challenges.stream()
                .map(challenge -> FriendChallengeListItemResponse.from(
                        challenge,
                        checkInService.getRanking(userId, challenge.id()).entries().stream()
                                .filter(entry -> userId.equals(entry.userId()))
                                .map(entry -> entry.position())
                                .findFirst()
                                .orElse(null)
                ))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FriendChallengeDetailResponse> getChallengeDetail(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {

        FriendChallengeDetailView detail = friendChallengeService.getChallengeDetail(userId, id);
        return ResponseEntity.ok(FriendChallengeDetailResponse.from(
                detail,
                checkInService.getRanking(userId, id),
                checkInService.listCheckIns(userId, id)
        ));
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
