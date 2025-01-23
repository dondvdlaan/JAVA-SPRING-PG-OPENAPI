package dev.manyroads.database;

import dev.manyroads.decomreception.DecomReceptionController;
import dev.manyroads.matterreception.MatterReceptionService;
import dev.manyroads.client.AdminClient;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterRequestCallback;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
import dev.manyroads.scheduler.SchedulerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Main test file
 */
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
    @MockitoBean
    AdminClient adminClient;
    @MockitoBean
    SchedulerService schedulerService;

    @Test
    @Transactional
    void customerExistChargeExistOtherVehicleTypeShouldNotAddMattterToExistingChargeTest() {
        // prepare
        long customerNr = (long) (Math.random() * 99999);
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr("121212");
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);
        when(adminClient.searchVehicleType(matterRequest.getMatterNr())).thenReturn("bulldozer");
        Customer existingCustomer = new Customer();
        existingCustomer.setCustomerNr(customerNr);
        customerRepository.save(existingCustomer);
        Charge existingCharge = new Charge();
        existingCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        existingCharge.setCustomerNr(matterRequest.getCustomerNr());
        existingCharge.setVehicleType(VehicleTypeEnum.DIRTBIKE);
        existingCharge.setCustomer(existingCustomer);
        Charge savedExistingCharge = chargeRepository.save(existingCharge);

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify
        verify(adminClient, times(1)).searchVehicleType(anyString());
        verify(schedulerService, times(1)).scheduleCustomerStandby(eq(customerNr));
        assertEquals(customerNr, matterResponse.getCustomerNr());
        assertNotEquals(savedExistingCharge.getChargeID(), matterResponse.getChargeID());
    }

    @Test
    @Transactional
    @DisplayName("Testing DB @Query for ChargeRepository")
    void checkIfChargeIsBookedTest() {
        // prepare
        long customerNr = (long) (Math.random() * 99999);
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr("121212");
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("xxx/yyy");
        matterRequest.setCallback(matterRequestCallback);
        Customer customer = new Customer();
        customer.setCustomerNr(matterRequest.getCustomerNr());
        Customer savedCustomer = customerRepository.save(customer);
        System.out.println("savedCustomer " + chargeRepository.findById(savedCustomer.getCustomerID()));
        Charge charge = new Charge();
        charge.setChargeStatus(ChargeStatusEnum.BOOKED);
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
        verify(schedulerService, times(1)).scheduleCustomerStandby(eq(customerNr));

    }

    @Test
    void chargeRepoTest() {

        // Prepare
        Customer customer = new Customer();
        customer.setCustomerNr((long) (Math.random() * 99999999));
        customerRepository.save(customer);
        Charge job = new Charge();
        ChargeStatusEnum status = ChargeStatusEnum.BOOKED;
        job.setChargeStatus(status);
        job.setCustomerNr(customer.getCustomerNr());
        job.setCustomer(customer);
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
