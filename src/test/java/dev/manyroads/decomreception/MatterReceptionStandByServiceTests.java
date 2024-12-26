package dev.manyroads.decomreception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.client.CustomerProcessingClient;
import dev.manyroads.matterreception.MatterReceptionService;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterRequestCallback;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
import dev.manyroads.scheduler.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MatterReceptionStandByServiceTests {

    MatterReceptionService matterReceptionService;
    AdminClient adminClient;
    CustomerRepository customerRepository;
    ChargeRepository chargeRepository;
    MatterRepository matterRepository;
    CustomerProcessingClient customerProcessingClient;
    SchedulerService schedulerService;

    @BeforeEach
    void preparation() {
        adminClient = mock(AdminClient.class);
        customerRepository = mock(CustomerRepository.class);
        chargeRepository = mock(ChargeRepository.class);
        matterRepository = mock(MatterRepository.class);
        customerProcessingClient = mock(CustomerProcessingClient.class);
        schedulerService = mock(SchedulerService.class);
        this.matterReceptionService = new MatterReceptionService(
                adminClient,
                customerRepository,
                chargeRepository,
                matterRepository,
                customerProcessingClient,
                schedulerService
        );
    }
    @Test
    @DisplayName("Existing customer, first MatterRequest, customer already in standby")
    void happyFlowExistingCustomerStandByTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        UUID customerID = UUID.randomUUID();
        String matterNr = "121212";
        String terminationCallBackUrl = "mooi/wel";
        VehicleTypeEnum vehicleTypeEnum = VehicleTypeEnum.BULLDOZER;

        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl(terminationCallBackUrl);
        matterRequest.setCallback(matterRequestCallback);

        when(adminClient.searchVehicleType(anyString())).thenReturn(String.valueOf(vehicleTypeEnum));

        Customer existingCustomer = Customer.builder()
                .customerID(customerID)
                .customerNr(customerNr)
                .standByFlag(true)
                .build();
        when(customerRepository.findByCustomerNr(anyLong())).thenReturn(existingCustomer);
        when(customerRepository.save(any())).thenReturn(existingCustomer);

        Charge existingCharge = new Charge();
        existingCharge.setChargeID(UUID.randomUUID());
        existingCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        existingCharge.setCustomerNr(matterRequest.getCustomerNr());
        existingCharge.setVehicleType(VehicleTypeEnum.DIRTBIKE);
        existingCharge.setCustomer(existingCustomer);
        Matter existingMatter = Matter.builder()
                .matterNr(matterRequest.getMatterNr())
                .charge(existingCharge)
                .build();
        existingCharge.getMatters().add(existingMatter);
        List<Charge> listCharge = new ArrayList<>();
        listCharge.add(existingCharge);
        when(chargeRepository.findByCustomerNrAndChargeStatus(any(), any(), anyLong())).thenReturn(Optional.of(listCharge));

        Charge newCharge = new Charge();
        UUID chargeID = UUID.randomUUID();
        newCharge.setChargeID(chargeID);
        newCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        newCharge.setCustomerNr(matterRequest.getCustomerNr());
        newCharge.setVehicleType(vehicleTypeEnum);
        newCharge.setCustomer(existingCustomer);
        when(chargeRepository.save(any())).thenReturn(newCharge);
        Matter newMatter = Matter.builder()
                .matterNr(matterRequest.getMatterNr())
                .charge(newCharge)
                .build();
        when(matterRepository.save(any())).thenReturn(newMatter);
        newCharge.getMatters().add(newMatter);
        when(chargeRepository.save(any())).thenReturn(newCharge);

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify
        verify(adminClient, times(1)).searchVehicleType(anyString());
        verify(customerRepository, times(1)).findByCustomerNr(anyLong());
        verify(chargeRepository, times(1)).findByCustomerNrAndChargeStatus(any(), any(), anyLong());
        verify(chargeRepository, times(2)).save(any());
        verify(matterRepository, times(1)).save(any());
        verify(customerRepository, times(0)).save(any());
        verify(schedulerService, times(0)).scheduleCustomerStandby(eq(customerNr));
        assertEquals(customerNr, matterResponse.getCustomerNr());
        assertEquals(chargeID, matterResponse.getChargeID());
    }

    @Test
    @DisplayName("New customer, first MatterRequest, customer goes in standby")
    void happyFlowStandByTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        UUID customerID = UUID.randomUUID();
        String matterNr = "121212";
        String terminationCallBackUrl = "mooi/wel";

        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl(terminationCallBackUrl);
        matterRequest.setCallback(matterRequestCallback);

        when(adminClient.searchVehicleType(anyString())).thenReturn("bulldozer");
        when(customerRepository.findByCustomerNr(anyLong())).thenReturn(null);

        Customer newCustomer = Customer.builder()
                .customerID(customerID)
                .customerNr(customerNr)
                .build();
        when(customerRepository.save(isA(Customer.class))).thenReturn(newCustomer);

        List<Charge> listCharge = new ArrayList<>();
        when(chargeRepository.findByCustomerNrAndChargeStatus(any(), any(), anyLong())).thenReturn(Optional.of(listCharge));

        Charge newCharge = new Charge();
        UUID chargeID = UUID.randomUUID();
        newCharge.setChargeID(chargeID);
        newCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        newCharge.setCustomerNr(matterRequest.getCustomerNr());
        newCharge.setVehicleType(VehicleTypeEnum.BULLDOZER);
        newCharge.setCustomer(newCustomer);
        when(chargeRepository.save(any())).thenReturn(newCharge);
        Matter newMatter = Matter.builder()
                .matterNr(matterNr)
                .charge(newCharge)
                .build();
        when(matterRepository.save(any())).thenReturn(newMatter);
        newCharge.getMatters().add(newMatter);
        when(chargeRepository.save(any())).thenReturn(newCharge);

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify
        verify(adminClient, times(1)).searchVehicleType(anyString());
        verify(customerRepository, times(1)).findByCustomerNr(anyLong());
        verify(chargeRepository, times(1)).findByCustomerNrAndChargeStatus(any(), any(), anyLong());
        verify(chargeRepository, times(2)).save(any());
        verify(matterRepository, times(1)).save(any());
        verify(customerRepository, times(2)).save(any());
        verify(schedulerService, times(1)).scheduleCustomerStandby(eq(customerNr));
        assertEquals(customerNr, matterResponse.getCustomerNr());
        assertEquals(chargeID, matterResponse.getChargeID());
    }
}
