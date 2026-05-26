package com.pacefriends.api.home.presentation;

import com.pacefriends.api.home.application.HomeSummaryService;
import com.pacefriends.api.home.domain.HomeSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/home-summary")
public class HomeSummaryController {

    private final HomeSummaryService homeSummaryService;

    public HomeSummaryController(HomeSummaryService homeSummaryService) {
        this.homeSummaryService = homeSummaryService;
    }

    @GetMapping
    public ResponseEntity<HomeSummaryResponse> getHomeSummary(
            @AuthenticationPrincipal UUID requestingUserId,
            @PathVariable UUID userId) {
        HomeSummary summary = homeSummaryService.getHomeSummary(requestingUserId, userId);
        return ResponseEntity.ok(HomeSummaryResponse.from(summary));
    }
}
