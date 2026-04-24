package br.com.sprj.admin.resource;

import br.com.sprj.admin.dto.*;
import br.com.sprj.admin.service.AdminService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/v1/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("super-admin")
@Tag(name = "Admin", description = "Gerenciamento de usuários, aplicações e roles (super-admin)")
@SecurityRequirement(name = "bearerAuth")
public class AdminResource {

    @Inject
    AdminService adminService;

    // ── Usuários ───────────────────────────────────────────────────────────────

    @GET
    @Path("/users")
    @Operation(summary = "Listar usuários")
    public List<AdminUserResponse> listUsers() {
        return adminService.listUsers();
    }

    @GET
    @Path("/users/{id}")
    @Operation(summary = "Detalhar usuário")
    public AdminUserResponse getUser(@PathParam("id") String id) {
        return adminService.getUser(id);
    }

    @PUT
    @Path("/users/{id}")
    @Operation(summary = "Editar usuário")
    public AdminUserResponse updateUser(
            @PathParam("id") String id,
            AdminUserRequest request) {
        return adminService.updateUser(id, request);
    }

    @PUT
    @Path("/users/{id}/status")
    @Operation(summary = "Bloquear ou desbloquear usuário")
    public Response updateUserStatus(
            @PathParam("id") String id,
            @Valid UserStatusRequest request) {
        adminService.updateUserStatus(id, request);
        return Response.noContent().build();
    }

    // ── Aplicações ─────────────────────────────────────────────────────────────

    @GET
    @Path("/applications")
    @Operation(summary = "Listar aplicações")
    public List<ApplicationResponse> listApplications() {
        return adminService.listApplications();
    }

    @POST
    @Path("/applications")
    @Operation(summary = "Criar aplicação")
    public Response createApplication(@Valid ApplicationRequest request) {
        adminService.createApplication(request);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/applications/{id}")
    @Operation(summary = "Remover aplicação")
    public Response deleteApplication(@PathParam("id") String id) {
        adminService.deleteApplication(id);
        return Response.noContent().build();
    }

    // ── Roles da aplicação ─────────────────────────────────────────────────────

    @GET
    @Path("/applications/{clientId}/roles")
    @Operation(summary = "Listar roles da aplicação")
    public List<RoleResponse> listApplicationRoles(@PathParam("clientId") String clientId) {
        return adminService.listApplicationRoles(clientId);
    }

    @POST
    @Path("/applications/{clientId}/roles")
    @Operation(summary = "Criar role na aplicação")
    public Response createApplicationRole(
            @PathParam("clientId") String clientId,
            @Valid RoleRequest request) {
        adminService.createApplicationRole(clientId, request);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/applications/{clientId}/roles/{roleName}")
    @Operation(summary = "Remover role da aplicação")
    public Response deleteApplicationRole(
            @PathParam("clientId") String clientId,
            @PathParam("roleName") String roleName) {
        adminService.deleteApplicationRole(clientId, roleName);
        return Response.noContent().build();
    }

    // ── Roles do usuário ───────────────────────────────────────────────────────

    @GET
    @Path("/users/{id}/roles")
    @Operation(summary = "Listar roles do usuário")
    public List<RoleResponse> listUserRoles(@PathParam("id") String id) {
        return adminService.listUserRoles(id);
    }

    @POST
    @Path("/users/{id}/roles")
    @Operation(summary = "Atribuir role ao usuário")
    public Response assignRoleToUser(
            @PathParam("id") String id,
            @Valid UserRoleRequest request) {
        adminService.assignRoleToUser(id, request);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/users/{id}/roles/{roleName}")
    @Operation(summary = "Remover role do usuário")
    public Response removeRoleFromUser(
            @PathParam("id") String id,
            @PathParam("roleName") String roleName,
            @QueryParam("clientId") String clientId) {
        adminService.removeRoleFromUser(id, roleName, clientId);
        return Response.noContent().build();
    }
}
