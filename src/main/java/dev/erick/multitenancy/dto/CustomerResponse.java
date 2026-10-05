package dev.erick.multitenancy.dto;

import dev.erick.multitenancy.model.Customer;

public record CustomerResponse(
        Long id,
        String name
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getName());
    }
}
