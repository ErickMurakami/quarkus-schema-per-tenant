package dev.erick.multitenancy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users", schema = "public")
public class User {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "tenant_schema", nullable = false)
    private String tenantSchema;

    protected User(){}

    public Long getId(){
        return id;
    }

    public String getEmail(){
        return email;
    }

    public String getTenantSchema(){
        return tenantSchema;
    }
}
