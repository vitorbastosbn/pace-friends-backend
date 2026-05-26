CREATE TABLE IF NOT EXISTS friend_challenge_check_ins (
    id                   UUID          NOT NULL DEFAULT gen_random_uuid(),
    friend_challenge_id  UUID          NOT NULL,
    user_id              UUID          NOT NULL,
    distance_km          NUMERIC(10,3) NOT NULL,
    duration_seconds     INTEGER       NOT NULL,
    pace_seconds_per_km  BIGINT        NOT NULL,
    check_in_date        DATE          NOT NULL,
    notes                TEXT,
    status               VARCHAR(20)   NOT NULL DEFAULT 'VALID',
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_fcci PRIMARY KEY (id),
    CONSTRAINT fk_fcci_challenge FOREIGN KEY (friend_challenge_id) REFERENCES friend_challenges(id),
    CONSTRAINT fk_fcci_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_fcci_challenge_id ON friend_challenge_check_ins (friend_challenge_id);
CREATE INDEX idx_fcci_user_id ON friend_challenge_check_ins (user_id);
CREATE INDEX idx_fcci_challenge_user_date ON friend_challenge_check_ins (friend_challenge_id, user_id, check_in_date);
