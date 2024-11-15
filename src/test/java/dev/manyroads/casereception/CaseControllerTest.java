package dev.manyroads.casereception;

import dev.manyroads.exception.CaseIDIsMissingException;
import dev.manyroads.exception.CaseRequestEmptyOrNullException;
import dev.manyroads.exception.PersonIDIsMissingException;
import dev.manyroads.model.CaseRequest;
import dev.manyroads.model.CaseResponse;
import dev.manyroads.verification.Verification;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CaseControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    Verification verification;

    @Test
    void caseRequestCaseIDNullShouldThrowExceptionTest()  {
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
    void caseRequestPersonIDNullShouldThrowExceptionTest()  {
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
    void caseRequestNulShouldThrowExceptionTest() throws Exception {
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
    void caseRequestShouldReturnStausCose200Test() throws Exception {
        // Prepare
        CaseRequest caseRequest = new CaseRequest();
        caseRequest.setPersonID(123456L);
        caseRequest.setCaseID("123456");
        CaseResponse caseResponse = new CaseResponse();
        String expected = "200 OK";

        // Activate
        ResponseEntity<CaseRequest> result = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/v1/cases",
                caseRequest,
                CaseRequest.class);

        // Verify
        assertEquals(result.getStatusCode().toString(), expected);
    }

    @Test
    void caseRequestShouldReturnCaseResponseTest() throws Exception {
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
