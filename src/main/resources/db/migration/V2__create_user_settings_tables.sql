CREATE TABLE IF NOT EXISTS user_settings (
    id               UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id          UUID        NOT NULL,
    objective        VARCHAR(50) NOT NULL,
    weekly_frequency VARCHAR(10) NOT NULL,
    effective_from   DATE        NOT NULL,
    created_at       TIMESTAMP   NOT NULL,
    updated_at       TIMESTAMP   NOT NULL,

    CONSTRAINT pk_user_settings PRIMARY KEY (id),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_user_settings_user_id ON user_settings (user_id);
CREATE INDEX IF NOT EXISTS idx_user_settings_effective_from ON user_settings (effective_from);

CREATE TABLE IF NOT EXISTS user_settings_audit (
    id                  UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id             UUID        NOT NULL,
    changed_field       VARCHAR(50) NOT NULL,
    old_value           VARCHAR(50),
    new_value           VARCHAR(50) NOT NULL,
    changed_at          TIMESTAMP   NOT NULL,
    changed_by_user_id  UUID        NOT NULL,

    CONSTRAINT pk_user_settings_audit PRIMARY KEY (id),
    CONSTRAINT fk_user_settings_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_user_settings_audit_user_id ON user_settings_audit (user_id);
