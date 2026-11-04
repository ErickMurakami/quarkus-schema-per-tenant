package dev.erick.multitenancy.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import dev.erick.multitenancy.model.Customer;
import dev.erick.multitenancy.repository.CustomerRepository;

class CustomerServiceTest {
    private final CustomerRepository repository = mock(CustomerRepository.class);
    private final CustomerService service = new CustomerService();

    CustomerServiceTest() { service.customerRepository = repository; }

    @Test
    void listsCustomersAndHandlesEmptyResults() {
        var customers = List.of(new Customer("Acme"), new Customer("Globex"));
        when(repository.findAll()).thenReturn(customers).thenReturn(List.of());
        assertEquals(customers, service.findAll());
        assertTrue(service.findAll().isEmpty());
    }

    @Test
    void savesCustomerWithRequestedName() {
        Customer customer = service.save("Acme");
        assertEquals("Acme", customer.getName());
        verify(repository).persist(customer);
    }

    @Test
    void propagatesPersistenceFailure() {
        doThrow(new IllegalStateException("database unavailable")).when(repository).persist(any());
        assertThrows(IllegalStateException.class, () -> service.save("Acme"));
    }
}
