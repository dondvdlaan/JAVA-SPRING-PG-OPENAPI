package dev.manyroads.utils;

import dev.manyroads.model.ChargeStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static dev.manyroads.model.ChargeStatusEnum.BOOKED;
import static dev.manyroads.model.ChargeStatusEnum.CUSTOMER_DECEASED;
import static dev.manyroads.model.ChargeStatusEnum.DCM_APPLIED;
import static dev.manyroads.model.ChargeStatusEnum.DONE;
import static dev.manyroads.model.ChargeStatusEnum.EXECUTABLE;
import static dev.manyroads.model.ChargeStatusEnum.PARTIALLY_EXECUTABLE;
import static dev.manyroads.model.ChargeStatusEnum.REJECTED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DCMStepFunctionsTest {

    static Stream<ChargeStatusEnum> allChargeStatuses() {
        return Stream.of(ChargeStatusEnum.values());
    }

    static Stream<ChargeStatusEnum> allChargeStatusesExceptBOOKED() {
        return Stream.of(REJECTED, DCM_APPLIED, DONE, CUSTOMER_DECEASED, EXECUTABLE, PARTIALLY_EXECUTABLE);
    }

    static Stream<ChargeStatusEnum> allChargeStatusesExceptDONE() {
        return Stream.of(BOOKED, REJECTED, DCM_APPLIED, CUSTOMER_DECEASED, EXECUTABLE, PARTIALLY_EXECUTABLE);
    }

    static Stream<ChargeStatusEnum> reducedStatuses() {
        return Stream.of(REJECTED, DCM_APPLIED, DONE, CUSTOMER_DECEASED);
    }

    static Stream<ChargeStatusEnum> terminationStatuses() {
        return Stream.of(REJECTED, DONE, CUSTOMER_DECEASED);
    }

    @ParameterizedTest
    @MethodSource("allChargeStatusesExceptDONE")
    @DisplayName("denied currentStatus = CUSTOMER_DECEASED")
    void deniedTransitionCUSTOMER_DECEASEDTest(ChargeStatusEnum desiredStatus) {
        // prepare
        ChargeStatusEnum currentStatus = CUSTOMER_DECEASED;
        // activate
        boolean result = DCMStepFunctions.isTransitionAllowed(currentStatus, desiredStatus);
        // Verify
        assertFalse(result);
    }

    @ParameterizedTest
    @MethodSource("reducedStatuses")
    @DisplayName("currentStatus = PARTIALLY_EXECUTABLE")
    void happyFlowTestParameterizedPARTIALLY_EXECUTABLE(ChargeStatusEnum desiredStatus) {
        // prepare
        ChargeStatusEnum currentStatus = PARTIALLY_EXECUTABLE;
        // activate
        boolean result = DCMStepFunctions.isTransitionAllowed(currentStatus, desiredStatus);
        // Verify
        assertTrue(result);
    }

    @ParameterizedTest
    @MethodSource("reducedStatuses")
    @DisplayName("currentStatus = EXECUTABLE")
    void happyFlowTestParameterizedEXECUTABLE(ChargeStatusEnum desiredStatus) {
        // prepare
        ChargeStatusEnum currentStatus = EXECUTABLE;
        // activate
        boolean result = DCMStepFunctions.isTransitionAllowed(currentStatus, desiredStatus);
        // Verify
        assertTrue(result);
    }

    @Test
    void happyFlowTestDONE() {
        // prepare
        ChargeStatusEnum currentStatus = CUSTOMER_DECEASED;
        ChargeStatusEnum desiredStatus = DONE;
        // activate
        boolean result = DCMStepFunctions.isTransitionAllowed(currentStatus, desiredStatus);
        // Verify
        assertTrue(result);
    }

    @ParameterizedTest
    @MethodSource("allChargeStatuses")
    @DisplayName("currentStatus = DONE")
    void deniedTransitionDONETest(ChargeStatusEnum desiredStatus) {
        // prepare
        ChargeStatusEnum currentStatus = DONE;
        // activate
        boolean result = DCMStepFunctions.isTransitionAllowed(currentStatus, desiredStatus);
        // Verify
        assertFalse(result);
    }

    @ParameterizedTest
    @MethodSource("terminationStatuses")
    @DisplayName("currentStatus = DCM_APPLIED")
    void happyFlowTestParameterizedDCM_APPLIED(ChargeStatusEnum desiredStatus) {
        // prepare
        ChargeStatusEnum currentStatus = DCM_APPLIED;
        // activate
        boolean result = DCMStepFunctions.isTransitionAllowed(currentStatus, desiredStatus);
        // Verify
        assertTrue(result);
    }

    @Test
    @DisplayName("currentStatus = REJECTED")
    void happyFlowTestRejected() {
        // prepare
        ChargeStatusEnum currentStatus = REJECTED;
        ChargeStatusEnum desiredStatus = DONE;
        // activate
        boolean result = DCMStepFunctions.isTransitionAllowed(currentStatus, desiredStatus);
        // Verify
        assertTrue(result);
    }

    @ParameterizedTest
    @MethodSource("allChargeStatusesExceptBOOKED")
    @DisplayName("currentStatus = BOOKED")
    void happyFlowTestParameterizedBooked(ChargeStatusEnum desiredStatus) {
        // prepare
        ChargeStatusEnum currentStatus = BOOKED;
        // activate
        boolean result = DCMStepFunctions.isTransitionAllowed(currentStatus, desiredStatus);
        // Verify
        assertTrue(result);
    }

    @Test
    void deniedTransitionTest() {
        // prepare
        ChargeStatusEnum currentStatus = REJECTED;
        ChargeStatusEnum desiredStatus = DCM_APPLIED;
        // activate
        boolean result = DCMStepFunctions.isTransitionAllowed(currentStatus, desiredStatus);
        // Verify
        assertFalse(result);
    }

    @Test
    void happyFlowTest() {
        // prepare
        ChargeStatusEnum currentStatus = BOOKED;
        ChargeStatusEnum desiredStatus = DCM_APPLIED;
        // activate
        boolean result = DCMStepFunctions.isTransitionAllowed(currentStatus, desiredStatus);
        // Verify
        assertTrue(result);
    }
}
