package dev.erick.multitenancy.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.Set;
import dev.erick.multitenancy.dto.LoginRequest;
import dev.erick.multitenancy.model.User;
import dev.erick.multitenancy.repository.UserRepository;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import jakarta.ws.rs.NotAuthorizedException;

class AuthServiceTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final AuthService service = new AuthService();

    AuthServiceTest() { service.userRepository = repository; }

    @Test
    void rejectsUnknownUser() {
        when(repository.findByEmail("unknown@demo.local")).thenReturn(Optional.empty());
        assertThrows(NotAuthorizedException.class,
                () -> service.login(new LoginRequest("unknown@demo.local", "password")));
    }

    @Test
    void rejectsIncorrectOrMissingPassword() {
        when(repository.findByEmail("user@demo.local")).thenReturn(Optional.of(mock(User.class)));
        for (String password : new String[]{null, "", "wrong", "Password"}) {
            assertThrows(NotAuthorizedException.class,
                    () -> service.login(new LoginRequest("user@demo.local", password)));
        }
    }

    @Test
    void issuesTokenWithUserIdentityRoleAndTenant() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        when(user.getEmail()).thenReturn("user@demo.local");
        when(user.getTenantSchema()).thenReturn("tenant_b");
        when(repository.findByEmail("user@demo.local")).thenReturn(Optional.of(user));
        JwtClaimsBuilder builder = mock(JwtClaimsBuilder.class, RETURNS_SELF);
        when(builder.sign()).thenReturn("signed-token");
        try (var jwt = mockStatic(Jwt.class)) {
            jwt.when(() -> Jwt.subject("42")).thenReturn(builder);
            var response = service.login(new LoginRequest("user@demo.local", "password"));
            assertEquals("signed-token", response.accessToken());
            assertEquals("tenant_b", response.tenant());
            verify(builder).upn("user@demo.local");
            verify(builder).groups(Set.of("user"));
            verify(builder).claim("tenant", "tenant_b");
            verify(builder).sign();
        }
    }
}
