package dev.erick.multitenancy.tenancy;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Set;

@PersistenceUnitExtension
@RequestScoped
public class JwtTenantResolver implements TenantResolver {

    private static final String DEFAULT_TENANT = "public";

    private static final Set<String> SUPPORTED_TENANTS = Set.of("public", "tenant1", "tenant2");

    @Inject
    SecurityIdentity identity;

    @Override
    public String getDefaultTenantId() {
        return DEFAULT_TENANT;
    }

    @Override
    public String resolveTenantId() {
        if(identity.isAnonymous()){
            return getDefaultTenantId();
        }

        if(!(identity.getPrincipal() instanceof JsonWebToken jwt)){
            throw new ForbiddenException("Invalid authentication context.");
        }

        String tenant = jwt.getClaim("tenant");

        if(tenant == null || !SUPPORTED_TENANTS.contains(tenant)){
            throw new ForbiddenException("Invalid tenant.");
        }

        return tenant;
    }
}
