package dev.manyroads.decomreception;

import dev.manyroads.matterreception.exception.MatterCallbackUrlIsMissingException;
import dev.manyroads.matterreception.exception.MatterIDIsMissingException;
import dev.manyroads.matterreception.exception.MatterRequestCustomerNrIsMissingException;
import dev.manyroads.matterreception.exception.MatterRequestEmptyOrNullException;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterRequestCallback;
import dev.manyroads.verification.Verification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

        // Verify
        assertThatThrownBy(() -> verification.verifyMatterRequest(matterRequestMatterIDNull))
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

        // Verify
        assertThatThrownBy(() -> verification.verifyMatterRequest(matterRequestCustomerNrNull))
                .isInstanceOf(MatterRequestCustomerNrIsMissingException.class)
                .hasMessage("DCM-002: MatterRequest CustomerNr is missing");
    }

    @Test
    void matterRequestCallbackNull() {
        Long customerNr = (long) (Math.random() * 999999);
        String matterNr = "121212";
        MatterRequest matterRequestCallbackNull = new MatterRequest();
        matterRequestCallbackNull.setMatterNr(matterNr);
        matterRequestCallbackNull.setCustomerNr(customerNr);
        matterRequestCallbackNull.setCallback(null);


        // Verify
        assertThatThrownBy(() -> verification.verifyMatterRequest(matterRequestCallbackNull))
                .isInstanceOf(MatterCallbackUrlIsMissingException.class)
                .hasMessage("DCM-005: MatterRequest callback URL is missing");
    }

    @Test
    void matterRequestCallbackUrlNull() {
        Long customerNr = (long) (Math.random() * 999999);
        String matterNr = "121212";
        MatterRequest matterRequestCallbackUrlNull = new MatterRequest();
        matterRequestCallbackUrlNull.setMatterNr(matterNr);
        matterRequestCallbackUrlNull.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl(null);
        matterRequestCallbackUrlNull.setCallback(matterRequestCallback);

        // Verify
        assertThatThrownBy(() -> verification.verifyMatterRequest(matterRequestCallbackUrlNull))
                .isInstanceOf(MatterCallbackUrlIsMissingException.class)
                .hasMessage("DCM-005: MatterRequest callback URL is missing");
    }

    @Test
    void matterRequestIsNull() {
        Long customerNr = (long) (Math.random() * 999999);
        String matterNr = "121212";
        MatterRequest matterRequestNull = null;

        // Verify
        assertThatThrownBy(() -> verification.verifyMatterRequest(matterRequestNull))
                .isInstanceOf(MatterRequestEmptyOrNullException.class)
                .hasMessage("DCM-001: CaseRequest empty or Null");
    }

    @Test
    void checkHappyFlowMatterReceptionVerificationTest() {
        Long customerNr = (long) (Math.random() * 999999);
        String matterNr = "121212";
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);

        // Verify
        Assertions.assertDoesNotThrow(() -> verification.verifyMatterRequest(matterRequest));
    }
}
