package br.com.sprj.auth.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "keycloak-token")
@Path("/realms/sprj/protocol/openid-connect")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public interface KeycloakTokenClient {

    @POST
    @Path("/token")
    TokenResponse token(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("refresh_token") String refreshToken
    );

    @POST
    @Path("/logout")
    void logout(
            @FormParam("client_id") String clientId,
            @FormParam("refresh_token") String refreshToken
    );
}
