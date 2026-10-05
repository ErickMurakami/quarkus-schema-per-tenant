package dev.erick.multitenancy.repository;

import dev.erick.multitenancy.model.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class CustomerRepository {

    @Inject
    EntityManager em;

    public List<Customer> findAll(){
        return em.createQuery("SELECT c FROM Customer c ORDER BY c.id", Customer.class).getResultList();
    }

    public Customer persist(Customer customer){
        em.persist(customer);
        return customer;
    }
}
