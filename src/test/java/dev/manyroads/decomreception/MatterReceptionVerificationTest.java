package dev.manyroads.decomreception;

import dev.manyroads.matterreception.exception.MatterIDIsMissingException;
import dev.manyroads.matterreception.exception.MatterRequestCustomerNrIsMissingException;
import dev.manyroads.matterreception.exception.MatterRequestEmptyOrNullException;
import dev.manyroads.matterreception.exception.MatterRequestHeaderEmptyOrNullException;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.verification.Verification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class MatterReceptionVerificationTest {

    Verification verification;

    @BeforeEach
    void setUp() {
        verification = new Verification();
    }

    @Test
    void matterRequestMatterIdIsMissing() {
        Long customerNr = (long) (Math.random() * 999999);
        String matterNr = "121212";
        MatterRequest matterRequestMatterIDNull = new MatterRequest();
        matterRequestMatterIDNull.setMatterNr(null);
        matterRequestMatterIDNull.setCustomerNr(customerNr);
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader("Termination-Call-Back-Url", "/v1/terminate-matter/");

        // Verify
        assertThatThrownBy(() -> verification.verifyMatterRequest(matterRequestMatterIDNull, httpServletRequest))
                .isInstanceOf(MatterIDIsMissingException.class)
                .hasMessage("DCM-003: CaseRequest CaseID is missing");
    }

    @Test
    void matterRequestCustomerNrNull() {
        Long customerNr = (long) (Math.random() * 999999);
        String matterNr = "121212";
        MatterRequest matterRequestCustomerNrNull = new MatterRequest();
        matterRequestCustomerNrNull.setMatterNr(matterNr);
        matterRequestCustomerNrNull.setCustomerNr(null);
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader("Termination-Call-Back-Url", "/v1/terminate-matter/");

        // Verify
        assertThatThrownBy(() -> verification.verifyMatterRequest(matterRequestCustomerNrNull, httpServletRequest))
                .isInstanceOf(MatterRequestCustomerNrIsMissingException.class)
                .hasMessage("DCM-002: MatterRequest CustomerNr is missing");
    }

    @Test
    void matterRequestIsNull() {
        Long customerNr = (long) (Math.random() * 999999);
        String matterNr = "121212";
        MatterRequest matterRequestNull = null;

        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addHeader("Termination-Call-Back-Url", "/v1/terminate-matter/");

        // Verify
        assertThatThrownBy(() -> verification.verifyMatterRequest(matterRequestNull, httpServletRequest))
                .isInstanceOf(MatterRequestEmptyOrNullException.class)
                .hasMessage("DCM-001: CaseRequest empty or Null");
    }

    @Test
    void matterRequestHeaderIsNull() {

        Long customerNr = (long) (Math.random() * 999999);
        String matterNr = "121212";
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MockHttpServletRequest requestWoHeader = new MockHttpServletRequest();
        requestWoHeader.addHeader("", "");

        // Verify
        assertThatThrownBy(() -> verification.verifyMatterRequest(matterRequest, requestWoHeader))
                .isInstanceOf(MatterRequestHeaderEmptyOrNullException.class)
                .hasMessage("DCM-005: matterRequest header empty or Null");
    }

    @Test
    void checkHappyFlowMatterReceptionVerificationTest() {
        Long customerNr = (long) (Math.random() * 999999);
        String matterNr = "121212";
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Termination-Call-Back-Url", "/v1/terminate-matter/");

        // Verify
        Assertions.assertDoesNotThrow(() -> verification.verifyMatterRequest(matterRequest, request));
    }
}
