package dev.manyroads.model.repository;

import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Customer findByCustomerNr(Long customerNr);


}
