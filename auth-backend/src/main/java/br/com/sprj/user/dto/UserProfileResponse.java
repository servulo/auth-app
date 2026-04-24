package br.com.sprj.user.dto;

import br.com.sprj.user.model.UserProfile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String keycloakId,
        String avatarUrl,
        String bio,
        Map<String, Object> preferences,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserProfileResponse from(UserProfile profile) {
        return new UserProfileResponse(
                profile.id,
                profile.keycloakId,
                profile.avatarUrl,
                profile.bio,
                profile.preferences,
                profile.createdAt,
                profile.updatedAt
        );
    }
}
