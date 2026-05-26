package com.pacefriends.api.trail.infrastructure;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class TrailStreakRepository {

    private final JdbcTemplate jdbcTemplate;

    public TrailStreakRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countCompletedStreakWeeks(UUID userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM weekly_streaks WHERE user_id = ? AND result = 'COMPLETED'",
                Integer.class, userId);
        return count == null ? 0 : count;
    }
}
