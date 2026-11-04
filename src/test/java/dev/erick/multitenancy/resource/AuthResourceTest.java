package dev.erick.multitenancy.resource;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import dev.erick.multitenancy.dto.LoginRequest;
import dev.erick.multitenancy.dto.LoginResponse;
import dev.erick.multitenancy.service.AuthService;
import jakarta.ws.rs.NotAuthorizedException;

class AuthResourceTest {
    @Test
    void returnsLoginResult() {
        AuthService service = mock(AuthService.class);
        AuthResource resource = new AuthResource();
        resource.authService = service;
        var request = new LoginRequest("user@demo.local", "password");
        var expected = new LoginResponse("token", "tenant_a");
        when(service.login(request)).thenReturn(expected);
        assertEquals(expected, resource.loginResponse(request));
    }

    @Test
    void propagatesInvalidCredentials() {
        AuthService service = mock(AuthService.class);
        AuthResource resource = new AuthResource();
        resource.authService = service;
        var request = new LoginRequest("user@demo.local", "wrong");
        when(service.login(request)).thenThrow(new NotAuthorizedException("Invalid credentials."));
        assertThrows(NotAuthorizedException.class, () -> resource.loginResponse(request));
    }
}
