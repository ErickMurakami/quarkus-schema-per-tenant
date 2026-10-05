package dev.erick.multitenancy.dto;

public record LoginResponse(
        String accessToken,
        String tenant
) {
}
