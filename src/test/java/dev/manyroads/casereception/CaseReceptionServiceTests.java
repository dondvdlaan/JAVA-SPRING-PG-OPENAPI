package dev.manyroads.casereception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.casereception.exception.AdminClientException;
import dev.manyroads.casereception.exception.VehicleTypeNotCoincideWithDomainException;
import dev.manyroads.casereception.exception.VehicleTypeNotFoundException;
import dev.manyroads.model.CaseRequest;
import dev.manyroads.model.CaseResponse;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CaseReceptionServiceTests {

    CaseReceptionService caseReceptionService;
    AdminClient adminClient;
    CustomerRepository customerRepository;
    ChargeRepository chargeRepository;

    @BeforeEach
    void preparation() {
        adminClient = mock(AdminClient.class);
        customerRepository = mock(CustomerRepository.class);
        chargeRepository = mock(ChargeRepository.class);
        this.caseReceptionService = new CaseReceptionService(adminClient, customerRepository, chargeRepository);
    }

    @Test
    void checkIfChargeIsBookedTest() {
        // prepare
        CaseRequest caseRequest = new CaseRequest();
        caseRequest.setCaseID("121212");
        caseRequest.setCustomerNr(343434L);
        Customer customer = new Customer();
        customer.setCustomerNr(caseRequest.getCustomerNr());
        Customer savedCustomer = customerRepository.save(customer);
        Charge charge = new Charge();
        charge.setChargeStatus("bookedd");
        charge.setCustomerNr(savedCustomer.getCustomerNr());
        charge.setCustomer(savedCustomer);
        Charge savedCharge = chargeRepository.save(charge);
        String expected = "bulldozer";
        when(adminClient.searchVehicleType(caseRequest.getCaseID())).thenReturn("bulldozer");

        // activate
        caseReceptionService.processIncomingCaseRequest(caseRequest);

        // verify

    }

    @Test
    void adminClientReturnsIncorrectVehicleTypeShouldThrowVehicleTypeNotCoincideExceptionTest() {
        // prepare
        CaseRequest caseRequest = new CaseRequest();
        caseRequest.setCaseID("121212");
        caseRequest.setCustomerNr(343434L);
        String expected = "bulldozer";
        when(adminClient.searchVehicleType(caseRequest.getCaseID())).thenReturn("Vouwfiets");

        // activate

        // verify
        assertThatThrownBy(() -> caseReceptionService.processIncomingCaseRequest(caseRequest))
                .isInstanceOf(VehicleTypeNotCoincideWithDomainException.class)
                .hasMessageStartingWith("DCM-006: Vehicle type does not coincide with domain");
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }

    @Test
    void adminClientReturnsFeignExceptionShouldThrowAdminClientExceptionTest() {
        // prepare
        CaseRequest caseRequest = new CaseRequest();
        caseRequest.setCaseID("121212");
        caseRequest.setCustomerNr(343434L);
        // Mock 404 BAD REQUEST return
        var feignException = Mockito.mock(FeignException.class);
        Mockito.when(feignException.status()).thenReturn(404);
        when(adminClient.searchVehicleType(caseRequest.getCaseID())).thenThrow(feignException);

        // activate

        // verify
        assertThatThrownBy(() -> caseReceptionService.processIncomingCaseRequest(caseRequest))
                .isInstanceOf(AdminClientException.class)
                .hasMessageStartingWith("DCM-004: No vehice type received");
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }

    @Test
    void adminClientReturnsNullShouldThrowVehicleTypeNotFoundExceptionTest() {
        // prepare
        CaseRequest caseRequest = new CaseRequest();
        caseRequest.setCaseID("121212");
        caseRequest.setCustomerNr(343434L);
        when(adminClient.searchVehicleType(caseRequest.getCaseID())).thenReturn(null);

        // activate

        // verify
        assertThatThrownBy(() -> caseReceptionService.processIncomingCaseRequest(caseRequest))
                .isInstanceOf(VehicleTypeNotFoundException.class)
                .hasMessageStartingWith("DCM-005: Vehicle type not found");
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }

    @Test
    void castIDShouldReturnCorrectVehicleTypeAndCustomerNrTest() {
        // prepare
        CaseRequest caseRequest = new CaseRequest();
        caseRequest.setCaseID("121212");
        caseRequest.setCustomerNr(343434L);
        String expectedVehicle = "bulldozer";
        when(adminClient.searchVehicleType(caseRequest.getCaseID())).thenReturn("bulldozer");

        // activate
        CaseResponse caseResponse = caseReceptionService.processIncomingCaseRequest(caseRequest);
        VehicleTypeEnum result = caseResponse.getVehicleType();

        // verify
        assertEquals(expectedVehicle, result.toString());
        assertEquals(caseRequest.getCustomerNr(), caseResponse.getCustomerNr());
        verify(adminClient, times(1)).searchVehicleType(anyString());
    }
}
