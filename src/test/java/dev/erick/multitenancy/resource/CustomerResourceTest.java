package dev.erick.multitenancy.resource;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import dev.erick.multitenancy.dto.CreateCustomerRequest;
import dev.erick.multitenancy.dto.CustomerResponse;
import dev.erick.multitenancy.model.Customer;
import dev.erick.multitenancy.service.CustomerService;

class CustomerResourceTest {
    private final CustomerService service = mock(CustomerService.class);
    private final CustomerResource resource = new CustomerResource();

    CustomerResourceTest() { resource.service = service; }

    private Customer customer(long id, String name) {
        Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn(id);
        when(customer.getName()).thenReturn(name);
        return customer;
    }

    @Test
    void listsCustomerResponsesInServiceOrder() {
        var customers = List.of(customer(2, "Acme"), customer(5, "Globex"));
        when(service.findAll()).thenReturn(customers);
        assertEquals(List.of(new CustomerResponse(2L, "Acme"), new CustomerResponse(5L, "Globex")), resource.list());
    }

    @Test
    void returnsEmptyListWhenNoCustomersExist() {
        when(service.findAll()).thenReturn(List.of());
        assertTrue(resource.list().isEmpty());
    }

    @Test
    void createsCustomerWithLocationAndResponseBody() {
        var customer = customer(7, "Acme");
        when(service.save("Acme")).thenReturn(customer);
        try (var response = resource.create(new CreateCustomerRequest("Acme"))) {
            assertEquals(201, response.getStatus());
            assertEquals("/customers/7", response.getLocation().toString());
            assertEquals(new CustomerResponse(7L, "Acme"), response.getEntity());
        }
    }
}
