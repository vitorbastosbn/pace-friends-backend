package com.pacefriends.api.auth;

import com.pacefriends.api.common.exception.UserConflictException;
import com.pacefriends.api.profile.domain.UserObjective;
import com.pacefriends.api.profile.domain.WeeklyFrequency;
import com.pacefriends.api.profile.infrastructure.UserSettingsRepository;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final JwtUtil jwtUtil;

    public AuthService(
            GoogleTokenVerifierService googleTokenVerifierService,
            UserRepository userRepository,
            UserSettingsRepository userSettingsRepository,
            JwtUtil jwtUtil) {
        this.googleTokenVerifierService = googleTokenVerifierService;
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponseDTO googleAuth(String idToken) {
        GoogleTokenInfo tokenInfo = googleTokenVerifierService.verify(idToken);

        User user = resolveUser(tokenInfo);

        String jwt = jwtUtil.generate(user.getId(), user.getName());

        return new AuthResponseDTO(
                jwt,
                new AuthResponseDTO.UserDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhotoUrl()
                )
        );
    }

    private User resolveUser(GoogleTokenInfo tokenInfo) {
        Optional<User> byGoogleId = userRepository.findByGoogleId(tokenInfo.googleId());

        if (byGoogleId.isPresent()) {
            log.debug("Existing user found by googleId: {}", tokenInfo.googleId());
            return byGoogleId.get();
        }

        Optional<User> byEmail = userRepository.findByEmail(tokenInfo.email());
        if (byEmail.isPresent()) {
            log.warn("Email {} already associated with a different googleId", tokenInfo.email());
            throw new UserConflictException(
                    "O e-mail " + tokenInfo.email() + " ja esta associado a outra conta.");
        }

        log.debug("Creating new user for googleId: {}", tokenInfo.googleId());
        User newUser = User.builder()
                .googleId(tokenInfo.googleId())
                .email(tokenInfo.email())
                .name(tokenInfo.name() != null ? tokenInfo.name() : tokenInfo.email())
                .photoUrl(tokenInfo.picture())
                .build();

        User saved = userRepository.save(newUser);
        userSettingsRepository.save(
                saved.getId(),
                UserObjective.IMPROVE_FITNESS,
                WeeklyFrequency.THREE,
                LocalDate.now());
        return saved;
    }
}
