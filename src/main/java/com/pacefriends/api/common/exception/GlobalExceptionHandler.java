package com.pacefriends.api.common.exception;

import com.pacefriends.api.challenge.domain.exception.ChallengeAccessDeniedException;
import com.pacefriends.api.challenge.domain.exception.ChallengeNotFoundException;
import com.pacefriends.api.friendchallenge.domain.exception.*;
import com.pacefriends.api.friendchallenge.domain.exception.CheckInAuditDateException;
import com.pacefriends.api.friendchallenge.domain.exception.DuplicateCheckInException;
import com.pacefriends.api.profile.domain.exception.ProfileAccessDeniedException;
import com.pacefriends.api.profile.domain.exception.UserNotFoundException;
import com.pacefriends.api.profile.domain.exception.UserSettingsNotFoundException;
import com.pacefriends.api.trail.domain.exception.LevelUpNotAllowedException;
import com.pacefriends.api.trail.domain.exception.TrailAccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Invalid token: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("invalid_token", ex.getMessage()));
    }

    @ExceptionHandler(UserConflictException.class)
    public ResponseEntity<ErrorResponse> handleUserConflict(UserConflictException ex) {
        log.warn("User conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("user_conflict", ex.getMessage()));
    }

    @ExceptionHandler(ChallengeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChallengeNotFound(ChallengeNotFoundException ex) {
        log.warn("Challenge not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("not_found", ex.getMessage()));
    }

    @ExceptionHandler(ChallengeAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleChallengeAccessDenied(ChallengeAccessDeniedException ex) {
        log.warn("Challenge access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("access_denied", ex.getMessage()));
    }

    @ExceptionHandler(ProfileAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleProfileAccessDenied(ProfileAccessDeniedException ex) {
        log.warn("Profile access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("access_denied", ex.getMessage()));
    }

    @ExceptionHandler(UserSettingsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserSettingsNotFound(UserSettingsNotFoundException ex) {
        log.warn("User settings not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("not_found", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("not_found", ex.getMessage()));
    }

    @ExceptionHandler(TrailAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleTrailAccessDenied(TrailAccessDeniedException ex) {
        log.warn("Trail access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("access_denied", ex.getMessage()));
    }

    @ExceptionHandler(LevelUpNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleLevelUpNotAllowed(LevelUpNotAllowedException ex) {
        log.warn("Level up not allowed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("level_up_not_allowed", ex.getMessage()));
    }

    // --- FriendChallenge exceptions ---

    @ExceptionHandler(InvalidInviteCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInviteCode(InvalidInviteCodeException ex) {
        log.warn("Invalid invite code: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("invite_code_not_found", ex.getMessage()));
    }

    @ExceptionHandler(ChallengeFullException.class)
    public ResponseEntity<ErrorResponse> handleChallengeFull(ChallengeFullException ex) {
        log.warn("Challenge full: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("challenge_full", ex.getMessage()));
    }

    @ExceptionHandler(AlreadyParticipantException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyParticipant(AlreadyParticipantException ex) {
        log.warn("Already participant: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("already_participant", ex.getMessage()));
    }

    @ExceptionHandler(ChallengeNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleChallengeNotActive(ChallengeNotActiveException ex) {
        log.warn("Challenge not active: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("challenge_not_active", ex.getMessage()));
    }

    @ExceptionHandler(FriendChallengeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFriendChallengeNotFound(FriendChallengeNotFoundException ex) {
        log.warn("Friend challenge not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("not_found", ex.getMessage()));
    }

    @ExceptionHandler(FriendChallengeAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleFriendChallengeAccessDenied(FriendChallengeAccessDeniedException ex) {
        log.warn("Friend challenge access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("access_denied", ex.getMessage()));
    }

    @ExceptionHandler(CheckInAuditDateException.class)
    public ResponseEntity<ErrorResponse> handleCheckInAuditDate(CheckInAuditDateException ex) {
        log.warn("Check-in on audit date: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("audit_date", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateCheckInException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateCheckIn(DuplicateCheckInException ex) {
        log.warn("Duplicate check-in: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("duplicate_check_in", ex.getMessage()));
    }

    @ExceptionHandler(CheckInNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCheckInNotFound(CheckInNotFoundException ex) {
        log.warn("Check-in not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("check_in_not_found", ex.getMessage()));
    }

    @ExceptionHandler(CheckInAlreadyRejectedException.class)
    public ResponseEntity<ErrorResponse> handleCheckInAlreadyRejected(CheckInAlreadyRejectedException ex) {
        log.warn("Check-in already rejected: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("check_in_already_rejected", ex.getMessage()));
    }

    @ExceptionHandler(ChallengeNotInAuditException.class)
    public ResponseEntity<ErrorResponse> handleChallengeNotInAudit(ChallengeNotInAuditException ex) {
        log.warn("Challenge not in audit: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("challenge_not_in_audit", ex.getMessage()));
    }

    // --- Generic ---

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedBody(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("bad_request", "Corpo da requisicao invalido ou mal formado."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getDefaultMessage())
                .findFirst()
                .orElse("Requisicao invalida.");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("bad_request", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("bad_request", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("internal_error", "Ocorreu um erro interno. Tente novamente."));
    }
}
