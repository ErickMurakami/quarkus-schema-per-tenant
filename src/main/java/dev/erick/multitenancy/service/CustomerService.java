package dev.erick.multitenancy.service;

import dev.erick.multitenancy.model.Customer;
import dev.erick.multitenancy.repository.CustomerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CustomerService {
    @Inject
    CustomerRepository customerRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Transactional
    public Customer save(String name){
        Customer customer = new Customer(name);
        customerRepository.persist(customer);
        return customer;
    }
}
