-- Drop existing FKs by looking them up dynamically (names may differ across environments),
-- then recreate with ON DELETE CASCADE.

DO $$
DECLARE r record;
BEGIN
    -- user_settings: user_id -> users
    FOR r IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'user_settings'::regclass
               AND confrelid = 'users'::regclass AND contype = 'f'
    LOOP EXECUTE 'ALTER TABLE user_settings DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP;

    -- user_settings_audit: user_id -> users
    FOR r IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'user_settings_audit'::regclass
               AND confrelid = 'users'::regclass AND contype = 'f'
    LOOP EXECUTE 'ALTER TABLE user_settings_audit DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP;

    -- challenges: user_id -> users
    FOR r IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'challenges'::regclass
               AND confrelid = 'users'::regclass AND contype = 'f'
    LOOP EXECUTE 'ALTER TABLE challenges DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP;

    -- activities: user_id -> users
    FOR r IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'activities'::regclass
               AND confrelid = 'users'::regclass AND contype = 'f'
    LOOP EXECUTE 'ALTER TABLE activities DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP;

    -- activities: challenge_id -> challenges
    FOR r IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'activities'::regclass
               AND confrelid = 'challenges'::regclass AND contype = 'f'
    LOOP EXECUTE 'ALTER TABLE activities DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP;

    -- friend_challenges: creator_id -> users
    FOR r IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'friend_challenges'::regclass
               AND confrelid = 'users'::regclass AND contype = 'f'
    LOOP EXECUTE 'ALTER TABLE friend_challenges DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP;

    -- friend_challenge_participants: user_id -> users
    FOR r IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'friend_challenge_participants'::regclass
               AND confrelid = 'users'::regclass AND contype = 'f'
    LOOP EXECUTE 'ALTER TABLE friend_challenge_participants DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP;

    -- friend_challenge_participants: friend_challenge_id -> friend_challenges
    FOR r IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'friend_challenge_participants'::regclass
               AND confrelid = 'friend_challenges'::regclass AND contype = 'f'
    LOOP EXECUTE 'ALTER TABLE friend_challenge_participants DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP;

    -- friend_challenge_check_ins: user_id -> users
    FOR r IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'friend_challenge_check_ins'::regclass
               AND confrelid = 'users'::regclass AND contype = 'f'
    LOOP EXECUTE 'ALTER TABLE friend_challenge_check_ins DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP;

    -- friend_challenge_check_ins: friend_challenge_id -> friend_challenges
    FOR r IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'friend_challenge_check_ins'::regclass
               AND confrelid = 'friend_challenges'::regclass AND contype = 'f'
    LOOP EXECUTE 'ALTER TABLE friend_challenge_check_ins DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP;

    -- user_achievements: user_id -> users
    FOR r IN SELECT conname FROM pg_constraint
             WHERE conrelid = 'user_achievements'::regclass
               AND confrelid = 'users'::regclass AND contype = 'f'
    LOOP EXECUTE 'ALTER TABLE user_achievements DROP CONSTRAINT ' || quote_ident(r.conname); END LOOP;
END $$;

-- Recreate all FKs with ON DELETE CASCADE

ALTER TABLE user_settings ADD CONSTRAINT fk_user_settings_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE user_settings_audit ADD CONSTRAINT fk_user_settings_audit_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE challenges ADD CONSTRAINT fk_challenges_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE activities ADD CONSTRAINT fk_activities_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE activities ADD CONSTRAINT fk_activities_challenge
    FOREIGN KEY (challenge_id) REFERENCES challenges(id) ON DELETE CASCADE;

ALTER TABLE friend_challenges ADD CONSTRAINT fk_fc_creator
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE friend_challenge_participants ADD CONSTRAINT fk_fcp_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE friend_challenge_participants ADD CONSTRAINT fk_fcp_challenge
    FOREIGN KEY (friend_challenge_id) REFERENCES friend_challenges(id) ON DELETE CASCADE;

ALTER TABLE friend_challenge_check_ins ADD CONSTRAINT fk_fcci_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE friend_challenge_check_ins ADD CONSTRAINT fk_fcci_challenge
    FOREIGN KEY (friend_challenge_id) REFERENCES friend_challenges(id) ON DELETE CASCADE;

ALTER TABLE user_achievements ADD CONSTRAINT fk_ua_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
