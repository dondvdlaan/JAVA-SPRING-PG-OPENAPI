package dev.manyroads.database;

import dev.manyroads.client.AdminClient;
import dev.manyroads.client.CustomerProcessingClient;
import dev.manyroads.decomreception.DecomReceptionController;
import dev.manyroads.matterreception.MatterReceptionService;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Copied from PostgresSqlTest for individual test
 */
@SpringBootTest
public class TestPostgresSqlTest {

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
    @MockBean
    CustomerProcessingClient customerProcessingClient;

    @Test
    @Transactional
    void customerExistChargeExistOtherVehicleTypeShouldNotAddMattterToExistingChargeTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr("121212");
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("tatata/wel");
        matterRequest.setCallback(matterRequestCallback);
        when(adminClient.searchVehicleType(matterRequest.getMatterNr())).thenReturn("bulldozer");
        when(customerProcessingClient.sendMessageToCustomerProcessing(any())).thenReturn(true);
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
        verify(customerProcessingClient, times(1)).sendMessageToCustomerProcessing(any());
        assertEquals(customerNr, matterResponse.getCustomerNr());
        assertNotEquals(savedExistingCharge.getChargeID(), matterResponse.getChargeID());
    }

    @Test
    void smokeTest() {
        // Verify
        assertThat(caseController).isNotNull();
    }
}
