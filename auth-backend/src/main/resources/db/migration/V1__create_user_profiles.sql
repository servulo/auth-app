CREATE TABLE user_profiles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id VARCHAR(36)  NOT NULL UNIQUE,
    avatar_url  VARCHAR(500),
    bio         TEXT,
    preferences JSONB,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
