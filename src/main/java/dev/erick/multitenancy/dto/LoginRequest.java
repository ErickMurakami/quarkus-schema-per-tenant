package dev.erick.multitenancy.dto;

public record LoginRequest(
        String email,
        String password
) {
}
