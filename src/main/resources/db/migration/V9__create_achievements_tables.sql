CREATE TABLE IF NOT EXISTS achievements (
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    slug           VARCHAR(100)  NOT NULL,
    name           VARCHAR(100)  NOT NULL,
    description    VARCHAR(255),
    criteria_type  VARCHAR(50)   NOT NULL,
    criteria_value INTEGER,
    icon_key       VARCHAR(50),
    CONSTRAINT pk_achievements PRIMARY KEY (id),
    CONSTRAINT uq_achievements_slug UNIQUE (slug)
);

CREATE TABLE IF NOT EXISTS user_achievements (
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    user_id        UUID          NOT NULL,
    achievement_id UUID          NOT NULL,
    unlocked_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_user_achievements PRIMARY KEY (id),
    CONSTRAINT fk_ua_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ua_achievement FOREIGN KEY (achievement_id) REFERENCES achievements(id),
    CONSTRAINT uq_ua_user_achievement UNIQUE (user_id, achievement_id)
);

CREATE INDEX idx_user_achievements_user_id ON user_achievements (user_id);
