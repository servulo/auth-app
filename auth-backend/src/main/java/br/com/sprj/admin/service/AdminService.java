package br.com.sprj.admin.service;

import br.com.sprj.admin.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@ApplicationScoped
public class AdminService {

    @Inject
    Keycloak keycloak;

    @ConfigProperty(name = "app.keycloak.realm")
    String realm;

    // ── Usuários ───────────────────────────────────────────────────────────────

    public List<AdminUserResponse> listUsers() {
        return keycloak.realm(realm).users().list()
                .stream().map(AdminUserResponse::from).toList();
    }

    public AdminUserResponse getUser(String id) {
        UserRepresentation user = keycloak.realm(realm).users().get(id).toRepresentation();
        if (user == null) throw new NotFoundException("Usuário não encontrado.");
        return AdminUserResponse.from(user);
    }

    public AdminUserResponse updateUser(String id, AdminUserRequest request) {
        var userResource = keycloak.realm(realm).users().get(id);
        UserRepresentation user = userResource.toRepresentation();
        if (user == null) throw new NotFoundException("Usuário não encontrado.");

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.email() != null) user.setEmail(request.email());

        userResource.update(user);
        return AdminUserResponse.from(userResource.toRepresentation());
    }

    public void updateUserStatus(String id, UserStatusRequest request) {
        var userResource = keycloak.realm(realm).users().get(id);
        UserRepresentation user = userResource.toRepresentation();
        if (user == null) throw new NotFoundException("Usuário não encontrado.");
        user.setEnabled(request.enabled());
        userResource.update(user);
    }

    // ── Aplicações (Clients) ───────────────────────────────────────────────────

    public List<ApplicationResponse> listApplications() {
        return keycloak.realm(realm).clients().findAll()
                .stream().map(ApplicationResponse::from).toList();
    }

    public void createApplication(ApplicationRequest request) {
        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(request.clientId());
        client.setName(request.name());
        client.setDescription(request.description());
        client.setEnabled(true);
        client.setPublicClient(true);
        client.setDirectAccessGrantsEnabled(false);
        keycloak.realm(realm).clients().create(client);
    }

    public void deleteApplication(String id) {
        keycloak.realm(realm).clients().get(id).remove();
    }

    // ── Roles ──────────────────────────────────────────────────────────────────

    public List<RoleResponse> listApplicationRoles(String clientId) {
        return getClientResource(clientId).roles().list()
                .stream().map(RoleResponse::from).toList();
    }

    public void createApplicationRole(String clientId, RoleRequest request) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(request.name());
        role.setDescription(request.description());
        getClientResource(clientId).roles().create(role);
    }

    public void deleteApplicationRole(String clientId, String roleName) {
        getClientResource(clientId).roles().deleteRole(roleName);
    }

    // ── Roles do usuário ───────────────────────────────────────────────────────

    public List<RoleResponse> listUserRoles(String userId) {
        return keycloak.realm(realm).users().get(userId)
                .roles().realmLevel().listAll()
                .stream().map(RoleResponse::from).toList();
    }

    public void assignRoleToUser(String userId, UserRoleRequest request) {
        ClientResource clientResource = getClientResource(request.clientId());
        RoleRepresentation role = clientResource.roles().get(request.roleName()).toRepresentation();
        keycloak.realm(realm).users().get(userId)
                .roles().clientLevel(getClientInternalId(request.clientId())).add(List.of(role));
    }

    public void removeRoleFromUser(String userId, String roleName, String clientId) {
        ClientResource clientResource = getClientResource(clientId);
        RoleRepresentation role = clientResource.roles().get(roleName).toRepresentation();
        keycloak.realm(realm).users().get(userId)
                .roles().clientLevel(getClientInternalId(clientId)).remove(List.of(role));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ClientResource getClientResource(String clientId) {
        List<ClientRepresentation> clients = keycloak.realm(realm).clients().findByClientId(clientId);
        if (clients.isEmpty()) throw new NotFoundException("Aplicação não encontrada: " + clientId);
        return keycloak.realm(realm).clients().get(clients.get(0).getId());
    }

    private String getClientInternalId(String clientId) {
        List<ClientRepresentation> clients = keycloak.realm(realm).clients().findByClientId(clientId);
        if (clients.isEmpty()) throw new NotFoundException("Aplicação não encontrada: " + clientId);
        return clients.get(0).getId();
    }
}
