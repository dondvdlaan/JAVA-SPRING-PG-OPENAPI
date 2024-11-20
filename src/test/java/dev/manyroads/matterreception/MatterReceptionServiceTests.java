package dev.manyroads.matterreception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.matterreception.exception.AdminClientException;
import dev.manyroads.matterreception.exception.VehicleTypeNotCoincideWithDomainException;
import dev.manyroads.matterreception.exception.VehicleTypeNotFoundException;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class MatterReceptionServiceTests {

    MatterReceptionService matterReceptionService;
    AdminClient adminClient;
    CustomerRepository customerRepository;
    ChargeRepository chargeRepository;

    @BeforeEach
    void preparation() {
        adminClient = mock(AdminClient.class);
        customerRepository = mock(CustomerRepository.class);
        chargeRepository = mock(ChargeRepository.class);
        this.matterReceptionService = new MatterReceptionService(adminClient, customerRepository, chargeRepository);
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
        assertThatThrownBy(() -> matterReceptionService.processIncomingCaseRequest(caseRequest))
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
        assertThatThrownBy(() -> matterReceptionService.processIncomingCaseRequest(caseRequest))
                .isInstanceOf(AdminClientException.class)
                .hasMessageStartingWith("DCM-004: No vehice type received");
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }

    @Test
    void adminClientReturnsNullShouldThrowVehicleTypeNotFoundExceptionTest() {
        // prepare
        MatterRequest caseRequest = new MatterRequest();
        caseRequest.setMatterID("121212");
        caseRequest.setCustomerNr(343434L);
        when(adminClient.searchVehicleType(caseRequest.getMatterID())).thenReturn(null);

        // activate

        // verify
        assertThatThrownBy(() -> matterReceptionService.processIncomingCaseRequest(caseRequest))
                .isInstanceOf(VehicleTypeNotFoundException.class)
                .hasMessageStartingWith("DCM-005: Vehicle type not found");
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }

    @Test
    void shouldReturnCorrectCustomerNrTest() {
        // prepare
        MatterRequest caseRequest = new MatterRequest();
        caseRequest.setMatterID("121212");
        caseRequest.setCustomerNr(343434L);
        String expectedVehicle = "bulldozer";
        when(adminClient.searchVehicleType(caseRequest.getMatterID())).thenReturn("bulldozer");

        // activate
        MatterResponse matterResponse = matterReceptionService.processIncomingCaseRequest(caseRequest);

        // verify
        assertEquals(caseRequest.getCustomerNr(), matterResponse.getCustomerNr());
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }
}
