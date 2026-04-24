package br.com.sprj.user.resource;

import br.com.sprj.user.dto.UserProfileRequest;
import br.com.sprj.user.dto.UserProfileResponse;
import br.com.sprj.user.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Perfil", description = "Gerenciamento de perfil do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UserResource {

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/{id}/profile")
    @Operation(summary = "Obter perfil do usuário")
    public UserProfileResponse getProfile(@PathParam("id") String id) {
        validateOwnership(id);
        return userService.getProfile(id);
    }

    @PUT
    @Path("/{id}/profile")
    @Operation(summary = "Atualizar perfil do usuário")
    public UserProfileResponse updateProfile(
            @PathParam("id") String id,
            UserProfileRequest request) {
        validateOwnership(id);
        return userService.updateProfile(id, request);
    }

    /**
     * Garante que o usuário só pode acessar/modificar o próprio perfil,
     * a menos que seja super-admin.
     */
    private void validateOwnership(String id) {
        String tokenSubject = jwt.getSubject();
        boolean isSuperAdmin = jwt.getGroups().contains("super-admin");
        if (!tokenSubject.equals(id) && !isSuperAdmin) {
            throw new ForbiddenException("Acesso negado.");
        }
    }
}
