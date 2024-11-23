package dev.manyroads.matterreception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.matterreception.exception.CaseIDIsMissingException;
import dev.manyroads.matterreception.exception.CaseRequestEmptyOrNullException;
import dev.manyroads.matterreception.exception.PersonIDIsMissingException;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.verification.Verification;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MatterReceptionControllerTests {

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
        MatterRequest caseRequestCaseIDIsNull = new MatterRequest();
        caseRequestCaseIDIsNull.setCustomerNr(987654L);
        caseRequestCaseIDIsNull.setMatterID(null);

        // Activate

        // Verify
        assertThatThrownBy(() -> verification.verifyCaseRequest(caseRequestCaseIDIsNull))
                .isInstanceOf(CaseIDIsMissingException.class)
                .hasMessageStartingWith("DCM-003: CaseRequest CaseID is missing");
    }

    @Test
    void caseRequestPersonIDNullShouldThrowExceptionTest() {
        // Prepare
        MatterRequest caseRequestPersonIDIsNull = new MatterRequest();
        caseRequestPersonIDIsNull.setMatterID(null);
        caseRequestPersonIDIsNull.setMatterID("123654");

        // Activate

        // Verify
        assertThatThrownBy(() -> verification.verifyCaseRequest(caseRequestPersonIDIsNull))
                .isInstanceOf(PersonIDIsMissingException.class)
                .hasMessageStartingWith("DCM-002: CaseRequest PersonID is missing");
    }

    @Test
    void caseRequestNulShouldThrowExceptionTest() {
        // Prepare
        MatterRequest caseRequestIsNull = null;
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "application/json");
        HttpEntity<MatterRequest> request = new HttpEntity<>(caseRequestIsNull, headers);
        String expectedStatusCode = "400 BAD_REQUEST";

        // Activate
        ResponseEntity<MatterRequest> result = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/v1/cases",
                request,
                MatterRequest.class);

        // Verify
        assertEquals(expectedStatusCode, result.getStatusCode().toString());
        assertThatThrownBy(() -> verification.verifyCaseRequest(caseRequestIsNull))
                .isInstanceOf(CaseRequestEmptyOrNullException.class)
                .hasMessageStartingWith("DCM-001: CaseRequest empty or Null");
    }

    @Test
    void caseRequestShouldReturnStatusCose200Test() {
        // Prepare
        MatterRequest caseRequest = new MatterRequest();
        caseRequest.setCustomerNr(123456L);
        caseRequest.setMatterID("123456");
        MatterResponse caseResponse = new MatterResponse();
        String expected = "200 OK";
        when(adminClient.searchVehicleType(caseRequest.getMatterID())).thenReturn("bulldozer");

        // Activate
        ResponseEntity<MatterRequest> result = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/v1/cases",
                caseRequest,
                MatterRequest.class);

        // Verify
        assertEquals(result.getStatusCode().toString(), expected);
    }

    @Test
    void caseRequestShouldReturnCaseResponseTest() {
        // Prepare
        MatterRequest caseRequest = new MatterRequest();
        MatterResponse caseResponse = new MatterResponse();
        String expected = caseResponse.toString();

        // Activate
        MatterResponse result = testRestTemplate.postForObject(
                "http://localhost:" + port + "/v1/cases",
                caseRequest,
                MatterResponse.class);

        // Verify
        assertEquals(result.toString(), expected);
    }
}
