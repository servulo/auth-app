package br.com.sprj.auth.service;

import br.com.sprj.auth.client.TokenResponse;
import br.com.sprj.auth.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuthService {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Keycloak keycloak;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.server-url")
    String keycloakServerUrl;

    @ConfigProperty(name = "app.keycloak.realm")
    String realm;

    @ConfigProperty(name = "app.keycloak.client-id")
    String clientId;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    // ── Token endpoint ─────────────────────────────────────────────────────────

    private TokenResponse callTokenEndpoint(Map<String, String> params) {
        String formBody = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new BadRequestException("Autenticação falhou: " + response.body());
            }
            return objectMapper.readValue(response.body(), TokenResponse.class);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o Keycloak.", e);
        }
    }

    private void callLogoutEndpoint(Map<String, String> params) {
        String formBody = params.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao comunicar com o Keycloak.", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // ── Auth operations ────────────────────────────────────────────────────────

    public LoginResponse login(LoginRequest request) {
        return toLoginResponse(callTokenEndpoint(Map.of(
                "grant_type", "password",
                "client_id", clientId,
                "username", request.username(),
                "password", request.password()
        )));
    }

    public void register(RegisterRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);
        user.setEmailVerified(true);

        Response response = keycloak.realm(realm).users().create(user);
        if (response.getStatus() == 409) {
            throw new BadRequestException("Usuário ou e-mail já cadastrado.");
        }

        String userId = CreatedResponseUtil.getCreatedId(response);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        keycloak.realm(realm).users().get(userId).resetPassword(credential);
    }

    public void logout(LogoutRequest request) {
        callLogoutEndpoint(Map.of(
                "client_id", clientId,
                "refresh_token", request.refreshToken()
        ));
    }

    public LoginResponse refresh(RefreshRequest request) {
        return toLoginResponse(callTokenEndpoint(Map.of(
                "grant_type", "refresh_token",
                "client_id", clientId,
                "refresh_token", request.refreshToken()
        )));
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        List<UserRepresentation> users = keycloak.realm(realm).users()
                .searchByEmail(request.email(), true);
        if (users.isEmpty()) return;
        keycloak.realm(realm).users().get(users.get(0).getId())
                .executeActionsEmail(List.of("UPDATE_PASSWORD"));
    }

    public void resetPassword(ResetPasswordRequest request) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.newPassword());
        keycloak.realm(realm).users().get(request.keycloakId()).resetPassword(credential);
    }

    private LoginResponse toLoginResponse(TokenResponse token) {
        return new LoginResponse(
                token.accessToken(),
                token.refreshToken(),
                token.expiresIn(),
                token.tokenType()
        );
    }
}
