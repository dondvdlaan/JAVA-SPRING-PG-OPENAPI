package dev.manyroads.database;

import dev.manyroads.decomreception.DecomReceptionController;
import dev.manyroads.matterreception.MatterReceptionService;
import dev.manyroads.client.AdminClient;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.enums.ChargeStatus;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@SpringBootTest
public class PostgresSqlTest {

    @Autowired
    ChargeRepository chargeRepository;
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    MatterRepository matterRepository;
    @Autowired
    DecomReceptionController caseController;
    @Autowired
    MatterReceptionService matterReceptionService;
    @MockBean
    AdminClient adminClient;

    @Disabled
    @Test
    @Transactional
    void customerExistChargeExistOtherVehicleTypeShouldNotAddMattterToExistingChargeTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr("121212");
        matterRequest.setCustomerNr(customerNr);
        when(adminClient.searchVehicleType(matterRequest.getMatterNr())).thenReturn("bulldozer");
        Customer existingCustomer = new Customer();
        existingCustomer.setCustomerNr(customerNr);
        customerRepository.save(existingCustomer);
        Charge existingCharge = new Charge();
        existingCharge.setChargeStatus(ChargeStatus.BOOKED);
        existingCharge.setCustomerNr(matterRequest.getCustomerNr());
        existingCharge.setVehicleType(VehicleTypeEnum.DIRTBIKE);
        existingCharge.setCustomer(existingCustomer);
        Charge savedExistingCharge = chargeRepository.save(existingCharge);

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify

        verify(adminClient, times(1)).searchVehicleType(anyString());
        assertEquals(customerNr, matterResponse.getCustomerNr());
        assertNotEquals(savedExistingCharge.getChargeID(), matterResponse.getChargeID());
    }

    @Test
    @Transactional
    @DisplayName("Testing DB @Query for ChargeRepository")
    void checkIfChargeIsBookedTest() {
        // prepare
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr("121212");
        matterRequest.setCustomerNr((long) (Math.random() * 9999999));
        Customer customer = new Customer();
        customer.setCustomerNr(matterRequest.getCustomerNr());
        Customer savedCustomer = customerRepository.save(customer);
        System.out.println("savedCustomer " + chargeRepository.findById(savedCustomer.getCustomerID()));
        Charge charge = new Charge();
        charge.setChargeStatus(ChargeStatus.BOOKED);
        charge.setCustomerNr(savedCustomer.getCustomerNr());
        charge.setCustomer(savedCustomer);
        charge.setVehicleType(VehicleTypeEnum.BULLDOZER);
        Charge savedCharge = chargeRepository.save(charge);
        String expected = "bulldozer";
        when(adminClient.searchVehicleType(matterRequest.getMatterNr())).thenReturn("bulldozer");

        // activate
        MatterResponse result = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify
        // Check log info messages

    }

    @Disabled
    @Test
    void chargeRepoTest() {

        // Prepare
        Customer customer = new Customer();
        customer.setCustomerNr((long) (Math.random() * 99999999));
        Customer customerSaved = customerRepository.save(customer);
        Charge job = new Charge();
        ChargeStatus status = ChargeStatus.BOOKED;
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
