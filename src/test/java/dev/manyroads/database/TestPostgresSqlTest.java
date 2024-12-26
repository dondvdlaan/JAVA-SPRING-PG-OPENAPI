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
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Copied from PostgresSqlTest for individual tests
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
    MatterReceptionService matterReceptionService;
    @Autowired
    DecomReceptionController decomReceptionController;
    @MockBean
    AdminClient adminClient;
    @MockBean
    CustomerProcessingClient customerProcessingClient;

    @BeforeEach
    void setUp() {
        matterRepository.deleteAll();
        chargeRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("Charge Repo Query test")
    void chargeRepoFindByCustomerAndChargeTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        String matterNr = "12345";
        ChargeStatusEnum chargeStatus = ChargeStatusEnum.BOOKED;
        Customer existingCustomer = Customer.builder().customerID(UUID.randomUUID()).customerNr(customerNr).charge(new ArrayList<>()).build();
        customerRepository.save(existingCustomer);
        Charge existingCharge = Charge.builder().chargeID(UUID.randomUUID()).customer(existingCustomer).chargeStatus(chargeStatus).customerNr(customerNr).vehicleType(VehicleTypeEnum.BULLDOZER).matters(new HashSet<>()).build();
        chargeRepository.save(existingCharge);
        Matter existingMatter = Matter.builder().matterNr(matterNr).charge(existingCharge).matterStatus(MatterStatus.EXECUTABLE).build();
        matterRepository.save(existingMatter);
        existingCharge.getMatters().add(existingMatter);
        chargeRepository.save(existingCharge);
        existingCustomer.getCharge().add(existingCharge);
        customerRepository.save(existingCustomer);

        Long customerNr2 = (long) (Math.random() * 99999);
        String matterNr2 = "67890";
        ChargeStatusEnum chargeStatus2 = ChargeStatusEnum.DCM_APPLIED;
        Customer existingCustomer2 = Customer.builder().customerID(UUID.randomUUID()).customerNr(customerNr2).charge(new ArrayList<>()).build();
        customerRepository.save(existingCustomer2);
        Charge existingCharge2 = Charge.builder().chargeID(UUID.randomUUID()).customer(existingCustomer2).chargeStatus(chargeStatus2).customerNr(customerNr2).vehicleType(VehicleTypeEnum.BULLDOZER).matters(new HashSet<>()).build();
        chargeRepository.save(existingCharge2);
        Matter existingMatter2 = Matter.builder().matterNr(matterNr2).charge(existingCharge2).matterStatus(MatterStatus.EXECUTABLE).build();
        matterRepository.save(existingMatter2);
        existingCharge2.getMatters().add(existingMatter2);
        chargeRepository.save(existingCharge2);
        existingCustomer2.getCharge().add(existingCharge2);
        customerRepository.save(existingCustomer2);

        // activate
        Optional<List<Charge>> oCharges = chargeRepository.findByCustomerNrAndChargeStatus(customerNr, ChargeStatusEnum.BOOKED);

        // verify
        assertThat(chargeRepository.findAll().size(), equalTo(2));
        if (oCharges.isPresent() && !oCharges.get().isEmpty()) {
            var charges = oCharges.get();
            assertThat(charges.size(), equalTo(1));
            charges.forEach(c -> {
                assertEquals(c.getCustomerNr(), customerNr);
                assertEquals(c.getChargeStatus(), chargeStatus);
            });
        }
        ;
    }

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
        org.assertj.core.api.Assertions.assertThat(decomReceptionController).isNotNull();
    }
}
