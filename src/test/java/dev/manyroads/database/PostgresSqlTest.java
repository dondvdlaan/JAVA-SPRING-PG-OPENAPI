package dev.manyroads.database;

import dev.manyroads.casereception.CaseReceptionController;
import dev.manyroads.casereception.CaseReceptionService;
import dev.manyroads.client.AdminClient;
import dev.manyroads.model.CaseRequest;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

@SpringBootTest
public class PostgresSqlTest {

    @Autowired
    ChargeRepository chargeRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    CaseReceptionController caseController;
    @Autowired
    CaseReceptionService caseReceptionService;
    @MockBean
    AdminClient adminClient;

    @Test
    @DisplayName("Testing DB @Query")
    void checkIfChargeIsBookedTest() {
        // prepare
        CaseRequest caseRequest = new CaseRequest();
        caseRequest.setCaseID("121212");
        caseRequest.setCustomerNr((long)(Math.random() * 9999999));
        Customer customer = new Customer();
        customer.setCustomerNr(caseRequest.getCustomerNr());
        Customer savedCustomer = customerRepository.save(customer);
        Charge charge = new Charge();
        charge.setChargeStatus("applied");
        charge.setCustomerNr(savedCustomer.getCustomerNr());
        charge.setCustomer(savedCustomer);
        Charge savedCharge = chargeRepository.save(charge);
        String expected = "bulldozer";
        when(adminClient.searchVehicleType(caseRequest.getCaseID())).thenReturn("bulldozer");

        // activate
        caseReceptionService.processIncomingCaseRequest(caseRequest);

        // verify

    }

    @Test
    void chargeRepoTest() {

        // Prepare
        Customer customer = new Customer();
        customer.setCustomerNr((long) (Math.random() * 99999999));
        Customer customerSaved = customerRepository.save(customer);
        Charge job = new Charge();
        String status = "recorded";
        job.setChargeStatus(status);
        job.setCustomerNr(customerSaved.getCustomerNr());
        job.setCustomer(customerSaved);
        Charge savedJob = chargeRepository.save(job);

        // Act
        Optional<Charge> oResult = chargeRepository.findById(savedJob.getChargeID());

        // Verify
        oResult.ifPresent(j -> assertEquals(status, j.getChargeStatus()));

    }

    @Test
    void smokeTest() {
        // Verify
        assertThat(caseController).isNotNull();
    }
}
