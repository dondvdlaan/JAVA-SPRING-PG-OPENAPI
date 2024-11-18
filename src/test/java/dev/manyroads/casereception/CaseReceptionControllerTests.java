package dev.manyroads.casereception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.exception.CaseIDIsMissingException;
import dev.manyroads.exception.CaseRequestEmptyOrNullException;
import dev.manyroads.exception.PersonIDIsMissingException;
import dev.manyroads.model.CaseRequest;
import dev.manyroads.model.CaseResponse;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.verification.Verification;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CaseReceptionControllerTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    Verification verification;

    @MockBean
    AdminClient adminClient;

    @Test
    void caseRequestCaseIDNullShouldThrowExceptionTest() {
        // Prepare
        CaseRequest caseRequestCaseIDIsNull = new CaseRequest();
        caseRequestCaseIDIsNull.setPersonID(987654L);
        caseRequestCaseIDIsNull.setCaseID(null);

        // Activate

        // Verify
        assertThatThrownBy(() -> verification.verifyCaseRequest(caseRequestCaseIDIsNull))
                .isInstanceOf(CaseIDIsMissingException.class)
                .hasMessageStartingWith("DCM-003: CaseRequest CaseID is missing");
    }

    @Test
    void caseRequestPersonIDNullShouldThrowExceptionTest() {
        // Prepare
        CaseRequest caseRequestPersonIDIsNull = new CaseRequest();
        caseRequestPersonIDIsNull.setPersonID(null);
        caseRequestPersonIDIsNull.setCaseID("123654");

        // Activate

        // Verify
        assertThatThrownBy(() -> verification.verifyCaseRequest(caseRequestPersonIDIsNull))
                .isInstanceOf(PersonIDIsMissingException.class)
                .hasMessageStartingWith("DCM-002: CaseRequest PersonID is missing");
    }

    @Test
    void caseRequestNulShouldThrowExceptionTest() {
        // Prepare
        CaseRequest caseRequestIsNull = null;
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "application/json");
        HttpEntity<CaseRequest> request = new HttpEntity<>(caseRequestIsNull, headers);
        String expectedStatusCode = "400 BAD_REQUEST";

        // Activate
        ResponseEntity<CaseRequest> result = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/v1/cases",
                request,
                CaseRequest.class);

        // Verify
        assertEquals(expectedStatusCode, result.getStatusCode().toString());
        assertThatThrownBy(() -> verification.verifyCaseRequest(caseRequestIsNull))
                .isInstanceOf(CaseRequestEmptyOrNullException.class)
                .hasMessageStartingWith("DCM-001: CaseRequest empty or Null");
    }

    @Test
    void caseRequestShouldReturnStatusCose200Test() {
        // Prepare
        CaseRequest caseRequest = new CaseRequest();
        caseRequest.setPersonID(123456L);
        caseRequest.setCaseID("123456");
        CaseResponse caseResponse = new CaseResponse();
        String expected = "200 OK";
        when(adminClient.searchVehicleType(caseRequest.getCaseID())).thenReturn("bulldozer");

        // Activate
        ResponseEntity<CaseRequest> result = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/v1/cases",
                caseRequest,
                CaseRequest.class);

        // Verify
        assertEquals(result.getStatusCode().toString(), expected);
    }

    @Test
    void caseRequestShouldReturnCaseResponseTest() {
        // Prepare
        CaseRequest caseRequest = new CaseRequest();
        CaseResponse caseResponse = new CaseResponse();
        String expected = caseResponse.toString();

        // Activate
        CaseResponse result = testRestTemplate.postForObject(
                "http://localhost:" + port + "/v1/cases",
                caseRequest,
                CaseResponse.class);

        // Verify
        assertEquals(result.toString(), expected);
    }
}
