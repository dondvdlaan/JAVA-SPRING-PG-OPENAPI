package dev.manyroads.matterreception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.client.CustomerProcessingClient;
import dev.manyroads.decomreception.exception.AdminClientException;
import dev.manyroads.matterreception.exception.VehicleTypeNotCoincideWithDomainException;
import dev.manyroads.decomreception.exception.VehicleTypeNotFoundException;
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
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MatterReceptionServiceTests {

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
    void customerExistChargeExistOtherVehicleTypeShouldCreateNewChargeTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        UUID customerID = UUID.randomUUID();
        String matterNr = "121212";
        VehicleTypeEnum existingVehicle = VehicleTypeEnum.DIRTBIKE;
        VehicleTypeEnum requestedVehicle = VehicleTypeEnum.BULLDOZER;

        when(adminClient.searchVehicleType(anyString())).thenReturn(requestedVehicle.toString());

        Customer existingCustomer = new Customer();
        existingCustomer.setCustomerID(customerID);
        existingCustomer.setCustomerNr(customerNr);
        existingCustomer.setStandByFlag(true);
        when(customerRepository.findByCustomerNr(anyLong())).thenReturn(existingCustomer);

        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);
        Charge existingCharge = new Charge();
        existingCharge.setChargeID(UUID.randomUUID());
        existingCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        existingCharge.setCustomerNr(customerNr);
        existingCharge.setVehicleType(existingVehicle);
        existingCharge.setCustomer(existingCustomer);
        List<Charge> listCharge = new ArrayList<>();
        listCharge.add(existingCharge);
        when(chargeRepository.findByCustomerNrAndChargeStatus(any(), any(), anyLong())).thenReturn(Optional.of(listCharge));

        Matter newMatter = Matter.builder()
                .matterNr(matterNr)
                .matterStatus(MatterStatus.EXECUTABLE)
                .build();
        Charge newCharge = new Charge();
        UUID newChargeID = UUID.randomUUID();
        newCharge.setChargeID(newChargeID);
        newCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        newCharge.setCustomerNr(customerNr);
        newCharge.setVehicleType(requestedVehicle);
        newCharge.setCustomer(existingCustomer);
        newCharge.getMatters().add(newMatter);
        when(chargeRepository.save(any())).thenReturn(newCharge);
        when(customerProcessingClient.sendMessageToCustomerProcessing(any())).thenReturn(true);

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify
        verify(adminClient, times(1)).searchVehicleType(anyString());
        verify(customerRepository, times(1)).findByCustomerNr(anyLong());
        verify(chargeRepository, times(1)).findByCustomerNrAndChargeStatus(any(), any(), anyLong());
        verify(chargeRepository, times(1)).save(any());
        verify(matterRepository, times(1)).save(any());
        verify(customerRepository, times(1)).save(any(Customer.class));
        assertEquals(customerNr, matterResponse.getCustomerNr());
    }

    @Test
    void customerNotExistsShoulCreateNewCustomerTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        UUID customerID = UUID.randomUUID();
        String matterNr = "121212";
        VehicleTypeEnum requestedVehicle = VehicleTypeEnum.BULLDOZER;

        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);

        when(adminClient.searchVehicleType(anyString())).thenReturn(requestedVehicle.toString());
        when(customerRepository.findByCustomerNr(anyLong())).thenReturn(null);

        Customer newCustomer = new Customer();
        newCustomer.setCustomerID(customerID);
        newCustomer.setCustomerNr(matterRequest.getCustomerNr());
        when(customerRepository.save(any())).thenReturn(newCustomer);

        List<Charge> listCharge = new ArrayList<>();
        when(chargeRepository.findByCustomerNrAndChargeStatus(any(), any(), anyLong())).thenReturn(Optional.of(listCharge));

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify
        verify(customerRepository, times(1)).findByCustomerNr(anyLong());
        verify(customerRepository, times(3)).save(any());
        verify(chargeRepository, times(1)).findByCustomerNrAndChargeStatus(any(), any(), anyLong());
        verify(chargeRepository, times(1)).save(any());
        verify(matterRepository, times(1)).save(any());
        assertEquals(customerNr, matterResponse.getCustomerNr());
    }

    @Test
    void customerExistShouldNotCreateNewCustomerTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        UUID customerID = UUID.randomUUID();
        String matterNr = "121212";

        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);
        when(adminClient.searchVehicleType(matterRequest.getMatterNr())).thenReturn("bulldozer");
        Customer existingCustomer = new Customer();
        existingCustomer.setCustomerID(customerID);
        existingCustomer.setCustomerNr(customerNr);
        when(customerRepository.findByCustomerNr(anyLong())).thenReturn(existingCustomer);

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify
        verify(customerRepository, times(1)).findByCustomerNr(anyLong());
        verify(customerRepository, times(2)).save(any(Customer.class));
    }

    @Test
    void adminClientReturnsIncorrectVehicleTypeShouldThrowVehicleTypeNotCoincideExceptionTest() {
        // prepare
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr("121212");
        matterRequest.setCustomerNr(343434L);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);
        String expected = "bulldozer";
        when(adminClient.searchVehicleType(matterRequest.getMatterNr())).thenReturn("Vouwfiets");

        // activate

        // verify
        assertThatThrownBy(() -> matterReceptionService.processIncomingMatterRequest(matterRequest))
                .isInstanceOf(VehicleTypeNotCoincideWithDomainException.class)
                .hasMessageStartingWith("DCM-006: Vehicle type does not coincide with domain");
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }

    @Test
    void adminClientReturnsFeignExceptionShouldThrowAdminClientExceptionTest() {
        // prepare
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr("121212");
        matterRequest.setCustomerNr(343434L);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);
        // Mock 404 BAD REQUEST return
        var feignException = Mockito.mock(FeignException.class);
        Mockito.when(feignException.status()).thenReturn(404);
        when(adminClient.searchVehicleType(matterRequest.getMatterNr())).thenThrow(feignException);

        // activate

        // verify
        assertThatThrownBy(() -> matterReceptionService.processIncomingMatterRequest(matterRequest))
                .isInstanceOf(AdminClientException.class)
                .hasMessageStartingWith("DCM-004: No vehice type received");
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }

    @Test
    void adminClientReturnsNullShouldThrowVehicleTypeNotFoundExceptionTest() {
        // prepare
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr("121212");
        matterRequest.setCustomerNr(343434L);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);
        when(adminClient.searchVehicleType(matterRequest.getMatterNr())).thenReturn(null);

        // activate

        // verify
        assertThatThrownBy(() -> matterReceptionService.processIncomingMatterRequest(matterRequest))
                .isInstanceOf(VehicleTypeNotFoundException.class)
                .hasMessageStartingWith("DCM-005: Vehicle type not found");
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }

    @Test
    void shouldReturnCorrectCustomerNrTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        UUID customerID = UUID.randomUUID();
        String matterNr = "121212";

        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);
        String expectedVehicle = "bulldozer";
        when(adminClient.searchVehicleType(matterRequest.getMatterNr())).thenReturn("bulldozer");

        Customer existingCustomer = new Customer();
        existingCustomer.setCustomerID(customerID);
        existingCustomer.setCustomerNr(customerNr);
        when(customerRepository.findByCustomerNr(anyLong())).thenReturn(existingCustomer);

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify
        assertEquals(matterRequest.getCustomerNr(), matterResponse.getCustomerNr());
        verify(adminClient, times(1)).searchVehicleType(anyString());
        verify(customerRepository, times(1)).findByCustomerNr(anyLong());
        verify(chargeRepository, times(1)).save(any());
        verify(customerRepository, times(2)).save(any(Customer.class));
    }
}
