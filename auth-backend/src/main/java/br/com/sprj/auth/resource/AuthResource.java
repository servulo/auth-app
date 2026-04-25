package br.com.sprj.auth.resource;

import br.com.sprj.auth.dto.*;
import br.com.sprj.auth.service.AuthService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Autenticação", description = "Endpoints públicos de autenticação")
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @Operation(summary = "Autenticar usuário")
    public LoginResponse login(@Valid LoginRequest request) {
        return authService.login(request);
    }

    @POST
    @Path("/register")
    @RolesAllowed("super-admin")
    @Operation(summary = "Cadastrar novo usuário (requer super-admin)")
    public Response register(@Valid RegisterRequest request) {
        authService.register(request);
        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/logout")
    @Operation(summary = "Encerrar sessão")
    public Response logout(@Valid LogoutRequest request) {
        authService.logout(request);
        return Response.noContent().build();
    }

    @POST
    @Path("/refresh")
    @Operation(summary = "Renovar token")
    public LoginResponse refresh(@Valid RefreshRequest request) {
        return authService.refresh(request);
    }

    @POST
    @Path("/forgot-password")
    @Operation(summary = "Solicitar recuperação de senha")
    public Response forgotPassword(@Valid ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return Response.noContent().build();
    }

    @POST
    @Path("/reset-password")
    @Operation(summary = "Redefinir senha")
    public Response resetPassword(@Valid ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Response.noContent().build();
    }
}
