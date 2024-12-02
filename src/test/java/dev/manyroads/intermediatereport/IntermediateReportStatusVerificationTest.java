package dev.manyroads.intermediatereport;

import dev.manyroads.intermediatereport.exception.IntermediateReportStatusEmptyOrNullException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMissingChargeNrException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMissingMattersException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMissingStatusException;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.IntermediateReportExplanationEnum;
import dev.manyroads.model.IntermediateReportMatterRequest;
import dev.manyroads.model.IntermediateReportStatusRequest;
import dev.manyroads.verification.Verification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class IntermediateReportStatusVerificationTest {

    static Stream<ChargeStatusEnum> chargeStatuses() {
        return Stream.of(ChargeStatusEnum.values());
    }

    static Stream<IntermediateReportExplanationEnum> matterExplanations() {
        return Stream.of(IntermediateReportExplanationEnum.values());
    }

    Verification verification;

    @BeforeEach
    void setUp() {
        verification = new Verification();
    }

    @ParameterizedTest
    @MethodSource("chargeStatuses")
    void intermediatReportStatusNullTest(ChargeStatusEnum chargeStatusEnum) {
        Long chargerNr = (long) (Math.random() * 999999);
        String matterNr = "1234567";
        IntermediateReportMatterRequest correctIntermediateReportMatterRequest = new IntermediateReportMatterRequest()
                .matterNr(matterNr)
                .intermediateReportExplanation(IntermediateReportExplanationEnum.RELEASED);
        IntermediateReportStatusRequest intermediateReportStatusNullRequest = null;

        // Verify
        assertThatThrownBy(() -> verification.verifyIntermediateReportStatus(intermediateReportStatusNullRequest))
                .isInstanceOf(IntermediateReportStatusEmptyOrNullException.class);
    }

    @ParameterizedTest
    @MethodSource("chargeStatuses")
    void mattersArrayEmptyIntermediatReportStatusTest(ChargeStatusEnum chargeStatusEnum) {
        Long chargerNr = (long) (Math.random() * 999999);
        String matterNr = "1234567";
        IntermediateReportMatterRequest correctIntermediateReportMatterRequest = new IntermediateReportMatterRequest()
                .matterNr(matterNr)
                .intermediateReportExplanation(IntermediateReportExplanationEnum.RELEASED);
        IntermediateReportStatusRequest mattersArrayEmptyNrNullIntermediateReportStatusRequest = new IntermediateReportStatusRequest()
                .chargeNr(chargerNr)
                .statusIntermediateReport(chargeStatusEnum)
                .mattersIntermediateReport(null);

        // Verify
        assertThatThrownBy(() -> verification.verifyIntermediateReportStatus(mattersArrayEmptyNrNullIntermediateReportStatusRequest))
                .isInstanceOf(IntermediateReportStatusMissingMattersException.class);
    }

    @ParameterizedTest
    @MethodSource("chargeStatuses")
    void statusNullIntermediatReportStatusTest(ChargeStatusEnum chargeStatusEnum) {
        Long chargerNr = (long) (Math.random() * 999999);
        String matterNr = "1234567";
        IntermediateReportMatterRequest correctIntermediateReportMatterRequest = new IntermediateReportMatterRequest()
                .matterNr(matterNr)
                .intermediateReportExplanation(IntermediateReportExplanationEnum.RELEASED);
        IntermediateReportStatusRequest statusNullIntermediateReportStatusRequest = new IntermediateReportStatusRequest()
                .chargeNr(chargerNr)
                .statusIntermediateReport(null)
                .addMattersIntermediateReportItem(correctIntermediateReportMatterRequest);

        // Verify
        assertThatThrownBy(() -> verification.verifyIntermediateReportStatus(statusNullIntermediateReportStatusRequest))
                .isInstanceOf(IntermediateReportStatusMissingStatusException.class);
    }

    @ParameterizedTest
    @MethodSource("chargeStatuses")
    void chargeNrNullIntermediatReportStatusTest(ChargeStatusEnum chargeStatusEnum) {
        Long chargerNr = (long) (Math.random() * 999999);
        String matterNr = "1234567";
        IntermediateReportMatterRequest correctIntermediateReportMatterRequest = new IntermediateReportMatterRequest()
                .matterNr(matterNr)
                .intermediateReportExplanation(IntermediateReportExplanationEnum.RELEASED);
        IntermediateReportStatusRequest matterNrNullIntermediateReportStatusRequest = new IntermediateReportStatusRequest()
                .chargeNr(null)
                .statusIntermediateReport(chargeStatusEnum)
                .addMattersIntermediateReportItem(correctIntermediateReportMatterRequest);

        // Verify
        assertThatThrownBy(() -> verification.verifyIntermediateReportStatus(matterNrNullIntermediateReportStatusRequest))
                .isInstanceOf(IntermediateReportStatusMissingChargeNrException.class);
    }

    @ParameterizedTest
    @MethodSource("chargeStatuses")
    void checkHappyFlowIntermediatReportStatusTest(ChargeStatusEnum chargeStatusEnum) {
        Long chargerNr = (long) (Math.random() * 999999);
        String matterNr = "1234567";
        IntermediateReportMatterRequest correctIntermediateReportMatterRequest = new IntermediateReportMatterRequest()
                .matterNr(matterNr)
                .intermediateReportExplanation(IntermediateReportExplanationEnum.RELEASED);
        IntermediateReportStatusRequest correctIntermediateReportStatusRequest = new IntermediateReportStatusRequest()
                .chargeNr(chargerNr)
                .statusIntermediateReport(chargeStatusEnum)
                .addMattersIntermediateReportItem(correctIntermediateReportMatterRequest);

        // Verify
        Assertions.assertDoesNotThrow(
                () -> verification.verifyIntermediateReportStatus(correctIntermediateReportStatusRequest));
    }

}
