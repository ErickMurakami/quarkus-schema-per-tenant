package dev.erick.multitenancy.tenancy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.ForbiddenException;
import org.eclipse.microprofile.jwt.JsonWebToken;

class JwtTenantResolverTest {
    private final SecurityIdentity identity = mock(SecurityIdentity.class);
    private final JwtTenantResolver resolver = new JwtTenantResolver();

    JwtTenantResolverTest() { resolver.identity = identity; }

    @Test
    void anonymousRequestsUsePublicSchema() {
        when(identity.isAnonymous()).thenReturn(true);
        assertEquals("public", resolver.getDefaultTenantId());
        assertEquals("public", resolver.resolveTenantId());
        verify(identity, never()).getPrincipal();
    }

    @Test
    void acceptsEachSupportedTenant() {
        JsonWebToken jwt = mock(JsonWebToken.class);
        when(identity.getPrincipal()).thenReturn(jwt);
        for (String tenant : new String[]{"tenant_a", "tenant_b", "public"}) {
            when(jwt.getClaim("tenant")).thenReturn(tenant);
            assertEquals(tenant, resolver.resolveTenantId());
        }
    }

    @Test
    void rejectsMissingOrUnsupportedTenant() {
        JsonWebToken jwt = mock(JsonWebToken.class);
        when(identity.getPrincipal()).thenReturn(jwt);
        for (String tenant : new String[]{null, "", "TENANT_A", "tenant_c", "tenant_a; DROP SCHEMA public"}) {
            when(jwt.getClaim("tenant")).thenReturn(tenant);
            assertThrows(ForbiddenException.class, resolver::resolveTenantId);
        }
    }

    @Test
    void rejectsNonJwtOrMissingPrincipal() {
        when(identity.getPrincipal()).thenReturn(() -> "user");
        assertThrows(ForbiddenException.class, resolver::resolveTenantId);
        when(identity.getPrincipal()).thenReturn(null);
        assertThrows(ForbiddenException.class, resolver::resolveTenantId);
    }
}
