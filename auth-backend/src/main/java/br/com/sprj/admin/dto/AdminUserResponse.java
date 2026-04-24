package br.com.sprj.admin.dto;

import org.keycloak.representations.idm.UserRepresentation;

public record AdminUserResponse(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Long createdTimestamp
) {
    public static AdminUserResponse from(UserRepresentation user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                Boolean.TRUE.equals(user.isEnabled()),
                user.getCreatedTimestamp()
        );
    }
}
