package dev.manyroads;

import dev.manyroads.casereception.CaseReceptionController;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class PostgresSqlTest {

    @Autowired
    ChargeRepository chargeRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    CaseReceptionController caseController;

    @Test
    void chargeRepoTest() {

        // Prepare
        Customer customer = new Customer();
        customer.setCustomerNr((long) (Math.random() * 99999999));
        Customer customerSaved = customerRepository.save(customer);
        Charge job = new Charge();
        String status = "recorded";
        job.setChargeStatus(status);
        job.setCustomer(customerSaved);
        Charge savedJob = chargeRepository.save(job);

        // Act
        Optional<Charge> oResult = chargeRepository.findById(savedJob.getChargeID());

        // Verify
        oResult.ifPresent(j -> assertEquals(status, j.getChargeStatus()));

    }
    @Test
    void smokeTest(){
        // Verify
        assertThat(caseController).isNotNull();
    }
}
