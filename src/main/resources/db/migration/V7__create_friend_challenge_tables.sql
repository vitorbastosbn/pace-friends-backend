CREATE TABLE IF NOT EXISTS friend_challenges (
    id             UUID          NOT NULL DEFAULT gen_random_uuid(),
    creator_id     UUID          NOT NULL,
    title          VARCHAR(100)  NOT NULL,
    description    TEXT,
    challenge_type VARCHAR(20)   NOT NULL,
    goal_value     NUMERIC(10,2),
    start_date     DATE          NOT NULL,
    end_date       DATE          NOT NULL,
    invite_code    VARCHAR(8)    NOT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT pk_friend_challenges PRIMARY KEY (id),
    CONSTRAINT fk_fc_creator FOREIGN KEY (creator_id) REFERENCES users(id),
    CONSTRAINT uq_fc_invite_code UNIQUE (invite_code)
);
CREATE INDEX idx_fc_creator_id ON friend_challenges (creator_id);
CREATE INDEX idx_fc_invite_code ON friend_challenges (invite_code);

CREATE TABLE IF NOT EXISTS friend_challenge_participants (
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    friend_challenge_id UUID        NOT NULL,
    user_id             UUID        NOT NULL,
    role                VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_fcp PRIMARY KEY (id),
    CONSTRAINT fk_fcp_challenge FOREIGN KEY (friend_challenge_id) REFERENCES friend_challenges(id),
    CONSTRAINT fk_fcp_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_fcp_challenge_user UNIQUE (friend_challenge_id, user_id)
);
CREATE INDEX idx_fcp_user_id ON friend_challenge_participants (user_id);
CREATE INDEX idx_fcp_challenge_id ON friend_challenge_participants (friend_challenge_id);
