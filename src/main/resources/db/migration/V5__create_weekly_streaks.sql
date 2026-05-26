CREATE TABLE IF NOT EXISTS weekly_streaks (
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID           NOT NULL REFERENCES users(id),
    week_start_date  DATE           NOT NULL,
    target_frequency INTEGER        NOT NULL,
    days_completed   INTEGER        NOT NULL,
    streak_count     INTEGER        NOT NULL,
    xp_earned        INTEGER        NOT NULL,
    result           VARCHAR(20)    NOT NULL,
    processed_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT pk_weekly_streaks PRIMARY KEY (id),
    CONSTRAINT uq_weekly_streaks_user_week UNIQUE (user_id, week_start_date)
);
CREATE INDEX IF NOT EXISTS idx_weekly_streaks_user_week ON weekly_streaks(user_id, week_start_date);
