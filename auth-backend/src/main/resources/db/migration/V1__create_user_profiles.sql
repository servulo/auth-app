CREATE TABLE user_profiles (
    id          UNIQUEIDENTIFIER  PRIMARY KEY DEFAULT NEWID(),
    keycloak_id VARCHAR(36)       NOT NULL UNIQUE,
    avatar_url  VARCHAR(500),
    bio         NVARCHAR(MAX),
    preferences NVARCHAR(MAX),
    created_at  DATETIME2         NOT NULL DEFAULT GETUTCDATE(),
    updated_at  DATETIME2         NOT NULL DEFAULT GETUTCDATE()
);
