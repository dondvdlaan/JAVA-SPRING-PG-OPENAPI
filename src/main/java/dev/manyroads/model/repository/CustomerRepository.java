package dev.manyroads.model.repository;

import dev.manyroads.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Customer findByCustomerNr(Long customerNr);
}
