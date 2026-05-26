CREATE TABLE IF NOT EXISTS challenges (
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID           NOT NULL,
    title            VARCHAR(100)   NOT NULL,
    goal_distance_km NUMERIC(8,2)   NOT NULL,
    deadline         DATE           NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT pk_challenges PRIMARY KEY (id),
    CONSTRAINT fk_challenges_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_challenges_user_id ON challenges (user_id);

CREATE TABLE IF NOT EXISTS activities (
    id                    UUID           NOT NULL DEFAULT gen_random_uuid(),
    challenge_id          UUID           NOT NULL,
    user_id               UUID           NOT NULL,
    distance_km           NUMERIC(8,3)   NOT NULL,
    duration_seconds      INTEGER        NOT NULL,
    pace_seconds_per_km   NUMERIC(8,2)   NOT NULL,
    activity_date         DATE           NOT NULL,
    notes                 TEXT,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT pk_activities PRIMARY KEY (id),
    CONSTRAINT fk_activities_challenge FOREIGN KEY (challenge_id) REFERENCES challenges(id),
    CONSTRAINT fk_activities_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_activities_challenge_id ON activities (challenge_id);
CREATE INDEX IF NOT EXISTS idx_activities_user_id ON activities (user_id);
