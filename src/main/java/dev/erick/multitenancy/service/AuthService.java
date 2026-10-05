package dev.erick.multitenancy.service;

import dev.erick.multitenancy.dto.LoginRequest;
import dev.erick.multitenancy.dto.LoginResponse;
import dev.erick.multitenancy.model.User;
import dev.erick.multitenancy.repository.UserRepository;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;

import java.util.Set;

@ApplicationScoped
public class AuthService {
    private static final String DEMO_PASSWORD = "password";

    @Inject
    UserRepository userRepository;

    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotAuthorizedException("Invalid credentials."));
        if(!DEMO_PASSWORD.equals(request.password())){
            throw new NotAuthorizedException("Invalid credentials.");
        }

        String token = Jwt.subject(user.getId().toString())
                .upn(user.getEmail())
                .groups(Set.of("user"))
                .claim("tenant", user.getTenantSchema())
                .sign();

        return new LoginResponse(token, user.getTenantSchema());
    }
}
