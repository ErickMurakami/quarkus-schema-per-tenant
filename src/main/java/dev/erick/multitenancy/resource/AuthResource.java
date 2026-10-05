package dev.erick.multitenancy.resource;

import dev.erick.multitenancy.dto.LoginRequest;
import dev.erick.multitenancy.dto.LoginResponse;
import dev.erick.multitenancy.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @PermitAll
    @Transactional
    public LoginResponse loginResponse(LoginRequest request){
        return authService.login(request);
    }
}
