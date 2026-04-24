package br.com.sprj.admin.dto;

import org.keycloak.representations.idm.ClientRepresentation;

public record ApplicationResponse(
        String id,
        String clientId,
        String name,
        String description,
        boolean enabled
) {
    public static ApplicationResponse from(ClientRepresentation client) {
        return new ApplicationResponse(
                client.getId(),
                client.getClientId(),
                client.getName(),
                client.getDescription(),
                Boolean.TRUE.equals(client.isEnabled())
        );
    }
}
