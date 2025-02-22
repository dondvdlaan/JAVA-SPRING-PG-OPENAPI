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
import dev.manyroads.scheduler.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Copied from PostgresSqlTest for individual tests
 */
@SpringBootTest
public class ChargePostgresSqlTest {

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
    @MockitoBean
    AdminClient adminClient;
    @MockitoBean
    SchedulerService schedulerService;
    @MockitoBean
    CustomerProcessingClient customerProcessingClient;

    @BeforeEach
    void setUp() {
        matterRepository.deleteAll();
        chargeRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("Customer and ChargeStatus Query test")
    void CustomerAndChargeStatusQueryTest() {
        // prepare
        long customerNr = (long) (Math.random() * 99999);
        String matterNr = "12345";
        ChargeStatusEnum chargeStatus = ChargeStatusEnum.BOOKED;
        Customer existingCustomer = Customer.builder()
                .customerNr(customerNr)
                .build();
        customerRepository.save(existingCustomer);
        Charge existingCharge = new Charge();
        existingCharge.setChargeStatus(chargeStatus);
        existingCharge.setVehicleType(VehicleTypeEnum.BULLDOZER);
        existingCharge.setCustomer(existingCustomer);
        chargeRepository.save(existingCharge);
        existingCustomer.getCharges().add(existingCharge);
        Matter existingMatter = Matter.builder().matterNr(matterNr).matterStatus(MatterStatus.EXECUTABLE).build();
        existingMatter.setCharge(existingCharge);
        matterRepository.save(existingMatter);
        existingCharge.getMatters().add(existingMatter);

        // activate
        Optional<List<Charge>> oCharges = chargeRepository.findByCustomerNrAndChargeStatus(
                chargeStatus, ChargeStatusEnum.CUSTOMER_DECEASED, customerNr);
        // verify
        oCharges.ifPresent(charges -> {
            System.out.println("Printing:");
            charges.forEach(System.out::println);
        });

        assertEquals(1, matterRepository.count());
        assertEquals(1, chargeRepository.count());
        assertEquals(1, customerRepository.count());
        assertEquals(false, oCharges.isEmpty());
        assertEquals(true, oCharges.isPresent());
        assertEquals(1, oCharges.get().size());
    }

    @Test
    @Transactional
    @DisplayName("Charge Repo Query test")
    void chargeRepoFindByCustomerAndChargeTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        String matterNr = "12345";
        ChargeStatusEnum chargeStatus = ChargeStatusEnum.BOOKED;
        Customer existingCustomer = Customer.builder().customerID(UUID.randomUUID()).customerNr(customerNr).charges(new ArrayList<>()).build();
        customerRepository.save(existingCustomer);
        Charge existingCharge = Charge.builder().chargeID(UUID.randomUUID()).customer(existingCustomer).chargeStatus(chargeStatus).vehicleType(VehicleTypeEnum.BULLDOZER).matters(new HashSet<>()).build();
        chargeRepository.save(existingCharge);
        Matter existingMatter = Matter.builder().matterNr(matterNr).charge(existingCharge).matterStatus(MatterStatus.EXECUTABLE).build();
        matterRepository.save(existingMatter);
        existingCharge.getMatters().add(existingMatter);
        chargeRepository.save(existingCharge);
        existingCustomer.getCharges().add(existingCharge);
        customerRepository.save(existingCustomer);

        Long customerNr2 = (long) (Math.random() * 99999);
        String matterNr2 = "67890";
        ChargeStatusEnum chargeStatus2 = ChargeStatusEnum.DCM_APPLIED;
        Customer existingCustomer2 = Customer.builder().customerID(UUID.randomUUID()).customerNr(customerNr2).charges(new ArrayList<>()).build();
        customerRepository.save(existingCustomer2);
        Charge existingCharge2 = Charge.builder().chargeID(UUID.randomUUID()).customer(existingCustomer2).chargeStatus(chargeStatus2).vehicleType(VehicleTypeEnum.BULLDOZER).matters(new HashSet<>()).build();
        chargeRepository.save(existingCharge2);
        Matter existingMatter2 = Matter.builder().matterNr(matterNr2).charge(existingCharge2).matterStatus(MatterStatus.EXECUTABLE).build();
        matterRepository.save(existingMatter2);
        existingCharge2.getMatters().add(existingMatter2);
        chargeRepository.save(existingCharge2);
        existingCustomer2.getCharges().add(existingCharge2);
        customerRepository.save(existingCustomer2);

        // activate
        Optional<List<Charge>> oCharges = chargeRepository.findByCustomerNrAndChargeStatuss(customerNr, ChargeStatusEnum.BOOKED);

        // verify
        assertThat(chargeRepository.findAll().size(), equalTo(2));
        if (oCharges.isPresent() && !oCharges.get().isEmpty()) {
            var charges = oCharges.get();
            assertThat(charges.size(), equalTo(1));
            charges.forEach(c -> {
                assertEquals(c.getCustomer().getCustomerNr(), customerNr);
                assertEquals(c.getChargeStatus(), chargeStatus);
            });
        }
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
        Customer existingCustomer = new Customer();
        existingCustomer.setCustomerNr(customerNr);
        customerRepository.save(existingCustomer);
        Charge existingCharge = new Charge();
        existingCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        existingCharge.setVehicleType(VehicleTypeEnum.DIRTBIKE);
        existingCharge.setCustomer(existingCustomer);
        Charge savedExistingCharge = chargeRepository.save(existingCharge);

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify
        verify(adminClient, times(1)).searchVehicleType(anyString());
        verify(schedulerService, times(1)).scheduleCustomerStandby(anyLong());
        assertTrue(customerRepository.findById(existingCustomer.getCustomerID()).get().isStandByFlag());
        assertEquals(customerNr, matterResponse.getCustomerNr());
        assertNotEquals(savedExistingCharge.getChargeID(), matterResponse.getChargeID());
    }

    @Test
    void smokeTest() {
        // Verify
        org.assertj.core.api.Assertions.assertThat(decomReceptionController).isNotNull();
    }
}
