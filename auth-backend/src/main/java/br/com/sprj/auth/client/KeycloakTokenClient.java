package br.com.sprj.auth.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "keycloak-token")
@Path("/realms/sprj/protocol/openid-connect")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public interface KeycloakTokenClient {

    @POST
    @Path("/token")
    TokenResponse token(MultivaluedMap<String, String> form);

    @POST
    @Path("/logout")
    void logout(MultivaluedMap<String, String> form);
}
