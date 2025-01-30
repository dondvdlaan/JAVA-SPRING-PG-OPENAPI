package dev.manyroads.utils;

import dev.manyroads.decomreception.exception.InternalTechnicalException;
import dev.manyroads.model.ChargeStatusEnum;

import static dev.manyroads.model.ChargeStatusEnum.CUSTOMER_DECEASED;
import static dev.manyroads.model.ChargeStatusEnum.DCM_APPLIED;
import static dev.manyroads.model.ChargeStatusEnum.DONE;
import static dev.manyroads.model.ChargeStatusEnum.EXECUTABLE;
import static dev.manyroads.model.ChargeStatusEnum.PARTIALLY_EXECUTABLE;
import static dev.manyroads.model.ChargeStatusEnum.REJECTED;

import java.util.List;
import java.util.Objects;

public class DCMStepFunctions {

    static public boolean isChargeTransitionAllowed(ChargeStatusEnum chargeStatus, ChargeStatusEnum intermediateReportStatus) {

        boolean allowed = false;

        var all = List.of(REJECTED, DCM_APPLIED, DONE, CUSTOMER_DECEASED, EXECUTABLE, PARTIALLY_EXECUTABLE);
        var terminating = List.of(REJECTED, DONE, CUSTOMER_DECEASED);
        var allReduced = List.of(REJECTED, DCM_APPLIED, DONE, CUSTOMER_DECEASED, PARTIALLY_EXECUTABLE);

        switch (chargeStatus) {
            case BOOKED -> allowed = all.contains(intermediateReportStatus);
            case REJECTED, CUSTOMER_DECEASED -> allowed = Objects.equals(DONE, intermediateReportStatus);
            case DCM_APPLIED -> allowed = terminating.contains(intermediateReportStatus);
            case DONE -> { // Do nothing, allowed is false
            }
            case EXECUTABLE, PARTIALLY_EXECUTABLE -> allowed = allReduced.contains(intermediateReportStatus);
            default -> throw new InternalTechnicalException("isTransitionAllowed: Default ChargeStatusEnum enums not matched ");
        }

        return allowed;
    }
}
