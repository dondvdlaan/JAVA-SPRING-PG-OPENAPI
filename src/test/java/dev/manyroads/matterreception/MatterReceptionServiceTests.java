package dev.manyroads.matterreception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.matterreception.exception.AdminClientException;
import dev.manyroads.matterreception.exception.VehicleTypeNotCoincideWithDomainException;
import dev.manyroads.matterreception.exception.VehicleTypeNotFoundException;
import dev.manyroads.model.ChargeStatus;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

    @BeforeEach
    void preparation() {
        adminClient = mock(AdminClient.class);
        customerRepository = mock(CustomerRepository.class);
        chargeRepository = mock(ChargeRepository.class);
        matterRepository = mock(MatterRepository.class);
        this.matterReceptionService = new MatterReceptionService(adminClient, customerRepository, chargeRepository, matterRepository);
    }

    @Test
    void customerExistShouldNotCreateNewCustomerTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        Customer existingCustomer = new Customer();
        existingCustomer.setCustomerNr(customerNr);
        when(customerRepository.findByCustomerNr(anyLong())).thenReturn(existingCustomer);
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterID("121212");
        matterRequest.setCustomerNr(customerNr);
        when(adminClient.searchVehicleType(matterRequest.getMatterID())).thenReturn("bulldozer");
        Charge newCharge = new Charge();
        newCharge.setChargeID(UUID.randomUUID());
        newCharge.setChargeStatus(ChargeStatus.BOOKED);
        newCharge.setCustomerNr(matterRequest.getCustomerNr());
        newCharge.setVehicleType(VehicleTypeEnum.BULLDOZER);
        newCharge.setCustomer(existingCustomer);
        newCharge.getMatters().add(Matter.builder().customerNr(matterRequest.getCustomerNr()).build());
        when(chargeRepository.save(any())).thenReturn(newCharge);

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify

        verify(customerRepository, times(1)).findByCustomerNr(anyLong());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void adminClientReturnsIncorrectVehicleTypeShouldThrowVehicleTypeNotCoincideExceptionTest() {
        // prepare
        MatterRequest caseRequest = new MatterRequest();
        caseRequest.setMatterID("121212");
        caseRequest.setCustomerNr(343434L);
        String expected = "bulldozer";
        when(adminClient.searchVehicleType(caseRequest.getMatterID())).thenReturn("Vouwfiets");

        // activate

        // verify
        assertThatThrownBy(() -> matterReceptionService.processIncomingMatterRequest(caseRequest))
                .isInstanceOf(VehicleTypeNotCoincideWithDomainException.class)
                .hasMessageStartingWith("DCM-006: Vehicle type does not coincide with domain");
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }

    @Test
    void adminClientReturnsFeignExceptionShouldThrowAdminClientExceptionTest() {
        // prepare
        MatterRequest caseRequest = new MatterRequest();
        caseRequest.setMatterID("121212");
        caseRequest.setCustomerNr(343434L);
        // Mock 404 BAD REQUEST return
        var feignException = Mockito.mock(FeignException.class);
        Mockito.when(feignException.status()).thenReturn(404);
        when(adminClient.searchVehicleType(caseRequest.getMatterID())).thenThrow(feignException);

        // activate

        // verify
        assertThatThrownBy(() -> matterReceptionService.processIncomingMatterRequest(caseRequest))
                .isInstanceOf(AdminClientException.class)
                .hasMessageStartingWith("DCM-004: No vehice type received");
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }

    @Test
    void adminClientReturnsNullShouldThrowVehicleTypeNotFoundExceptionTest() {
        // prepare
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterID("121212");
        matterRequest.setCustomerNr(343434L);
        when(adminClient.searchVehicleType(matterRequest.getMatterID())).thenReturn(null);

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
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterID("121212");
        matterRequest.setCustomerNr(343434L);
        String expectedVehicle = "bulldozer";
        when(adminClient.searchVehicleType(matterRequest.getMatterID())).thenReturn("bulldozer");
        Charge newCharge = new Charge();
        newCharge.setChargeID(UUID.randomUUID());
        newCharge.setChargeStatus(ChargeStatus.BOOKED);
        newCharge.setCustomerNr(matterRequest.getCustomerNr());
        newCharge.setVehicleType(VehicleTypeEnum.BULLDOZER);
        newCharge.getMatters().add(Matter.builder().customerNr(matterRequest.getCustomerNr()).build());
        when(chargeRepository.save(any())).thenReturn(newCharge);

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        // verify
        assertEquals(matterRequest.getCustomerNr(), matterResponse.getCustomerNr());
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }
}
