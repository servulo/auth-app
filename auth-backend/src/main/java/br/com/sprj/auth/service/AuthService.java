package br.com.sprj.auth.service;

import br.com.sprj.auth.client.KeycloakTokenClient;
import br.com.sprj.auth.client.TokenResponse;
import br.com.sprj.auth.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import jakarta.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
public class AuthService {

    @RestClient
    KeycloakTokenClient tokenClient;

    @Inject
    Keycloak keycloak;

    @ConfigProperty(name = "app.keycloak.realm")
    String realm;

    @ConfigProperty(name = "app.keycloak.client-id")
    String clientId;

    public LoginResponse login(LoginRequest request) {
        MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
        form.add("grant_type", "password");
        form.add("client_id", clientId);
        form.add("username", request.username());
        form.add("password", request.password());
        return toLoginResponse(tokenClient.token(form));
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
        MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
        form.add("client_id", clientId);
        form.add("refresh_token", request.refreshToken());
        tokenClient.logout(form);
    }

    public LoginResponse refresh(RefreshRequest request) {
        MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", clientId);
        form.add("refresh_token", request.refreshToken());
        return toLoginResponse(tokenClient.token(form));
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        List<UserRepresentation> users = keycloak.realm(realm).users()
                .searchByEmail(request.email(), true);
        if (users.isEmpty()) return;
        keycloak.realm(realm).users().get(users.get(0).getId())
                .executeActionsEmail(List.of("UPDATE_PASSWORD"));
    }

    /**
     * Redefine a senha diretamente via Keycloak Admin Client.
     * Em produção, este endpoint deve ser protegido por um token de ação
     * gerado pelo Keycloak e enviado por e-mail ao usuário.
     */
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
