package dev.manyroads.decomreception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.client.CustomerProcessingClient;
import dev.manyroads.matterreception.MatterReceptionService;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateNewChargeTest {
    MatterReceptionService matterReceptionService;
    AdminClient adminClient;
    CustomerRepository customerRepository;
    ChargeRepository chargeRepository;
    MatterRepository matterRepository;
    CustomerProcessingClient customerProcessingClient;

    @BeforeEach
    void preparation() {
        adminClient = mock(AdminClient.class);
        customerRepository = mock(CustomerRepository.class);
        chargeRepository = mock(ChargeRepository.class);
        matterRepository = mock(MatterRepository.class);
        customerProcessingClient=mock(CustomerProcessingClient.class);
        this.matterReceptionService = new MatterReceptionService(
                adminClient, customerRepository, chargeRepository, matterRepository,customerProcessingClient);
    }

    @Disabled
    @Test
    void createNewChargeShouldReturnCharge() {
        Long customerNr = (long) (Math.random() * 99999);
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setCustomerNr(customerNr);
        UUID customerID = UUID.randomUUID();
        Customer existingCustomer = new Customer();
        existingCustomer.setCustomerID(customerID);
        existingCustomer.setCustomerNr(customerNr);
        Charge newCharge = new Charge();
        UUID chargeID = UUID.randomUUID();
        newCharge.setChargeID(chargeID);
        newCharge.setChargeStatus(ChargeStatusEnum.BOOKED_);
        newCharge.setCustomerNr(matterRequest.getCustomerNr());
        newCharge.setVehicleType(VehicleTypeEnum.BULLDOZER);
        newCharge.setCustomer(existingCustomer);
        when(chargeRepository.save(any())).thenReturn(newCharge);
        Matter matter = Matter.builder()
                .customerNr(matterRequest.getCustomerNr())
                .charge(newCharge)
                .build();
        when(matterRepository.save(any())).thenReturn(matter);
        newCharge.getMatters().add(matter);
        when(chargeRepository.save(any())).thenReturn(newCharge);

        //Charge result = matterReceptionService.createNewCharge(matterRequest, VehicleTypeEnum.BULLDOZER, existingCustomer);

        verify(chargeRepository, times(2)).save(any());
        verify(matterRepository, times(1)).save(any());
        verify(customerRepository, never()).save(any());
        //assertNotNull(result);


    }
}