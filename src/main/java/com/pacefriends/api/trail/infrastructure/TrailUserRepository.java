package com.pacefriends.api.trail.infrastructure;

import com.pacefriends.api.profile.domain.exception.UserNotFoundException;
import com.pacefriends.api.user.User;
import com.pacefriends.api.user.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class TrailUserRepository {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public TrailUserRepository(UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public TrailUserStats getStats(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Long activityCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM activities WHERE user_id = ?",
                Long.class, userId);

        Double totalDistanceKm = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(distance_km), 0) FROM activities WHERE user_id = ?",
                Double.class, userId);

        return new TrailUserStats(
                activityCount == null ? 0L : activityCount,
                totalDistanceKm == null ? 0.0 : totalDistanceKm,
                user.getCurrentStreak(),
                user.getTotalXp(),
                user.getCurrentLevel()
        );
    }

    public User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
