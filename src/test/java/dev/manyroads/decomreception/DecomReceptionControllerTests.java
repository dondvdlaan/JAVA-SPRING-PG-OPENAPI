package dev.manyroads.decomreception;

import dev.manyroads.matterreception.exception.MatterIDIsMissingException;
import dev.manyroads.matterreception.exception.MatterRequestEmptyOrNullException;
import dev.manyroads.matterreception.exception.MatterRequestCustomerNrIsMissingException;
import dev.manyroads.matterreception.MatterReceptionService;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterRequestCallback;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.verification.Verification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import  org. springframework. test. context. bean. override. mockito. MockitoBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.Objects;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DecomReceptionControllerTests {

    private static final String CLIENT_NAME = "decom";
    private static final String CLIENT_PASSWORD = "secret";
    @LocalServerPort
    private int port;

    @Autowired
    DecomReceptionController decomReceptionController;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    Verification verification;

    @MockitoBean
    MatterReceptionService matterReceptionService;

    @Test
    void caseRequestCaseIDNullShouldThrowExceptionTest() {
        // Prepare
        MatterRequest matterRequestMatterIDIsNull = new MatterRequest();
        matterRequestMatterIDIsNull.setCustomerNr(987654L);
        matterRequestMatterIDIsNull.setMatterNr(null);

        // Activate

        // Verify
        assertThatThrownBy(() -> verification.verifyMatterRequest(matterRequestMatterIDIsNull))
                .isInstanceOf(MatterIDIsMissingException.class)
                .hasMessageStartingWith("DCM-003: CaseRequest CaseID is missing");
    }

    @Test
    void caseRequestPersonIDNullShouldThrowExceptionTest() {
        // Prepare
        MatterRequest caseRequestPersonIDIsNull = new MatterRequest();
        caseRequestPersonIDIsNull.setMatterNr(null);

        // Activate

        // Verify
        assertThatThrownBy(() -> verification.verifyMatterRequest(caseRequestPersonIDIsNull))
                .isInstanceOf(MatterRequestCustomerNrIsMissingException.class)
                .hasMessageStartingWith("DCM-002: MatterRequest CustomerNr is missing");
    }

    @Test
    void matterRequestNulShouldThrowExceptionTest() {
        // Prepare
        MatterRequest matterRequestIsNull = null;
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", "application/json");
        HttpEntity<MatterRequest> request = new HttpEntity<>(matterRequestIsNull, headers);
        String expectedStatusCode = "400 BAD_REQUEST";

        // Activate
        ResponseEntity<MatterResponse> result = testRestTemplate
                .withBasicAuth(CLIENT_NAME, CLIENT_PASSWORD)
                .postForEntity(
                "http://localhost:" + port + "/v1/matters",
                request,
                MatterResponse.class);

        // Verify
        System.out.println("result: " + result);
        assertEquals(expectedStatusCode, result.getStatusCode().toString());
        assertThatThrownBy(() -> verification.verifyMatterRequest(matterRequestIsNull))
                .isInstanceOf(MatterRequestEmptyOrNullException.class)
                .hasMessageStartingWith("DCM-001: CaseRequest empty or Null");
    }

    @Test
    void matterRequestShouldReturnStatusCode200Test() {
        // Prepare
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setCustomerNr(123456L);
        matterRequest.setMatterNr("7890123");
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("een/eindpunt");
        matterRequest.setCallback(matterRequestCallback);
        UUID chargeID = UUID.randomUUID();
        MatterResponse matterResponse = new MatterResponse();
        matterResponse.setChargeID(chargeID);
        matterResponse.setCustomerNr(matterRequest.getCustomerNr());
        String expected = "200 OK";
        when(matterReceptionService.processIncomingMatterRequest(any())).thenReturn(matterResponse);

        // Activate
        ResponseEntity<MatterResponse> result = testRestTemplate
                .withBasicAuth(CLIENT_NAME,CLIENT_PASSWORD)
                .postForEntity(
                "http://localhost:" + port + "/v1/matters",
                matterRequest,
                MatterResponse.class);

        // Verify
        assertEquals(result.getStatusCode().toString(), expected);
        assertEquals(Objects.requireNonNull(result.getBody()).getCustomerNr(), matterRequest.getCustomerNr());
    }

    @Test
    @DisplayName("HappyFlow")
    void matterRequestShouldReturnMatterResponseTest() {
        // Prepare
        Long customerNr = (long) (Math.random() * 99999);
        String terminationCallBackUrl = "/v1/terminate-matter/";

        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr("12345");
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);
        MatterResponse matterResponse = new MatterResponse();
        matterResponse.setCustomerNr(customerNr);
        Long expected = matterResponse.getCustomerNr();
        when(matterReceptionService.processIncomingMatterRequest(any())).thenReturn(matterResponse);

        // Activate
        MatterResponse result = testRestTemplate
                .withBasicAuth(CLIENT_NAME,CLIENT_PASSWORD)
                .postForObject(
                "http://localhost:" + port + "/v1/matters",
                matterRequest,
                MatterResponse.class);

        // Verify
        System.out.println("expected: " + expected);
        System.out.println("result: " + result);
        verify(matterReceptionService, times(1)).processIncomingMatterRequest(any());
        assertEquals(expected, result.getCustomerNr());
    }
}
