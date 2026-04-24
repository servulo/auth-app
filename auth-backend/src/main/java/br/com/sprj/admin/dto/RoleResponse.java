package br.com.sprj.admin.dto;

import org.keycloak.representations.idm.RoleRepresentation;

public record RoleResponse(
        String id,
        String name,
        String description
) {
    public static RoleResponse from(RoleRepresentation role) {
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }
}
