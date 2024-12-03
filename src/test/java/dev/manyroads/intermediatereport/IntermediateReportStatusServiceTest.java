package dev.manyroads.intermediatereport;

import dev.manyroads.client.AdminClient;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusChargeIDNotExistException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusChargeTerminatedException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMattersNotBelongToChargeException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMissingChargeNrException;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.IntermediateReportExplanationEnum;
import dev.manyroads.model.IntermediateReportMatterRequest;
import dev.manyroads.model.IntermediateReportStatusRequest;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.MatterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IntermediateReportStatusServiceTest {

    IntermediateReportStatusService intermediateReportStatusService;
    AdminClient adminClient;
    ChargeRepository chargeRepository;
    MatterRepository matterRepository;

    @BeforeEach
    void setUp() {
        adminClient = mock(AdminClient.class);
        chargeRepository = mock(ChargeRepository.class);
        matterRepository = mock(MatterRepository.class);
        this.intermediateReportStatusService = new IntermediateReportStatusService(
                adminClient, chargeRepository, matterRepository);
    }

    @Test
    void mattersNoBelongToChargeThrowsExceptionTest() {
        UUID chargeID = UUID.randomUUID();
        String matterNr = "12345";

        Matter matter = Matter.builder()
                .matterNr(matterNr)
                .build();
        Set<Matter> setMatters = new HashSet<>();
        setMatters.add(matter);
        Charge charge = Charge.builder()
                .chargeID(chargeID)
                .chargeStatus(ChargeStatusEnum.EXECUTABLE)
                .matters(setMatters)
                .build();
        when(chargeRepository.findById(any())).thenReturn(Optional.of(charge));

        IntermediateReportStatusRequest intermediateReportStatusRequest = new IntermediateReportStatusRequest()
                .chargeID(chargeID)
                .statusIntermediateReport(ChargeStatusEnum.PARTIALLY_EXECUTABLE)
                .addMattersIntermediateReportItem(
                        new IntermediateReportMatterRequest()
                                .matterNr("67890")
                                .intermediateReportExplanation(IntermediateReportExplanationEnum.RELEASED));

        // Activate
        assertThatThrownBy(() -> intermediateReportStatusService.processIntermediateReportStatusRequests(intermediateReportStatusRequest))
                .isInstanceOf(IntermediateReportStatusMattersNotBelongToChargeException.class)
                .hasMessage(MessageFormat.format("DCM-306: Matters {0} do not belong to charge {1}.", List.of("67890"), charge.getChargeID()));

        // assert
        verify(chargeRepository, times(1)).findById(any());
        verify((adminClient), never()).startExecutable(any());
        verify((adminClient), never()).startDCMApplied(any());
        verify(matterRepository, never()).save(eq(matter));
    }

    @Test
    void chargeNotFoundThrowsExceptionTest() {
        UUID chargeID = UUID.randomUUID();
        String matterNr = "12345";

        Matter matter = Matter.builder()
                .matterNr(matterNr)
                .build();
        Set<Matter> setMatters = new HashSet<>();
        setMatters.add(matter);
        Charge charge = Charge.builder()
                .chargeID(chargeID)
                .chargeStatus(ChargeStatusEnum.EXECUTABLE)
                .matters(setMatters)
                .build();
        when(chargeRepository.findById(any())).thenReturn(Optional.empty());

        IntermediateReportStatusRequest intermediateReportStatusRequest = new IntermediateReportStatusRequest()
                .chargeID(chargeID)
                .statusIntermediateReport(ChargeStatusEnum.PARTIALLY_EXECUTABLE)
                .addMattersIntermediateReportItem(
                        new IntermediateReportMatterRequest()
                                .matterNr(matterNr)
                                .intermediateReportExplanation(IntermediateReportExplanationEnum.RELEASED));

        // Activate
        assertThatThrownBy(() -> intermediateReportStatusService.processIntermediateReportStatusRequests(intermediateReportStatusRequest))
                .isInstanceOf(IntermediateReportStatusChargeIDNotExistException.class)
                .hasMessage(MessageFormat.format("DCM-305: ChargeID {0} does not exist.", intermediateReportStatusRequest.getChargeID().toString()));

        // assert
        verify(chargeRepository, times(1)).findById(any());
        verify((adminClient), never()).startExecutable(any());
        verify((adminClient), never()).startDCMApplied(any());
        verify(matterRepository, never()).save(eq(matter));
    }

    @Test
    void chargeDONEThrowsExceptionTest() {
        UUID chargeID = UUID.randomUUID();
        String matterNr = "12345";

        Matter matter = Matter.builder()
                .matterNr(matterNr)
                .build();
        Set<Matter> setMatters = new HashSet<>();
        setMatters.add(matter);
        Charge charge = Charge.builder()
                .chargeID(chargeID)
                .chargeStatus(ChargeStatusEnum.DONE)
                .matters(setMatters)
                .build();
        when(chargeRepository.findById(any())).thenReturn(Optional.of(charge));

        IntermediateReportStatusRequest intermediateReportStatusRequest = new IntermediateReportStatusRequest()
                .chargeID(chargeID)
                .statusIntermediateReport(ChargeStatusEnum.PARTIALLY_EXECUTABLE)
                .addMattersIntermediateReportItem(
                        new IntermediateReportMatterRequest()
                                .matterNr(matterNr)
                                .intermediateReportExplanation(IntermediateReportExplanationEnum.RELEASED));

        // Activate
        assertThatThrownBy(() -> intermediateReportStatusService.processIntermediateReportStatusRequests(intermediateReportStatusRequest))
                .isInstanceOf(IntermediateReportStatusChargeTerminatedException.class)
                .hasMessage(MessageFormat.format("DCM-306: Charge {0} terminated with state {1}.", charge.getChargeID(), charge.getChargeStatus()));

        // assert
        verify(chargeRepository, times(1)).findById(any());
        verify((adminClient), never()).startExecutable(any());
        verify((adminClient), never()).startDCMApplied(any());
        verify(matterRepository, never()).save(eq(matter));
    }

    @Test
    void happyFlowPARTIALLY_EXECUTABLETest() {
        UUID chargeID = UUID.randomUUID();
        String matterNr = "12345";

        Matter matter = Matter.builder()
                .matterNr(matterNr)
                .build();
        Set<Matter> setMatters = new HashSet<>();
        setMatters.add(matter);
        Charge charge = Charge.builder()
                .chargeStatus(ChargeStatusEnum.EXECUTABLE)
                .matters(setMatters)
                .build();
        when(chargeRepository.findById(any())).thenReturn(Optional.of(charge));
        when(matterRepository.findByMatterNrAndCharge(eq(matterNr), eq(charge))).thenReturn(Optional.of(matter));
        IntermediateReportStatusRequest intermediateReportStatusRequest = new IntermediateReportStatusRequest()
                .chargeID(chargeID)
                .statusIntermediateReport(ChargeStatusEnum.PARTIALLY_EXECUTABLE)
                .addMattersIntermediateReportItem(
                        new IntermediateReportMatterRequest()
                                .matterNr(matterNr)
                                .intermediateReportExplanation(IntermediateReportExplanationEnum.RELEASED));

        // Activate
        intermediateReportStatusService.processIntermediateReportStatusRequests(intermediateReportStatusRequest);

        // assert
        verify(chargeRepository, times(1)).findById(any());
        verify((adminClient), never()).startExecutable(any());
        verify((adminClient), never()).startDCMApplied(any());
        verify(matterRepository, times(1)).save(eq(matter));
    }

    @Test
    void happyFlowDCMAppliedTest() {
        UUID chargeID = UUID.randomUUID();
        String matterNr = "12345";

        Charge charge = Charge.builder()
                .chargeStatus(ChargeStatusEnum.EXECUTABLE)
                .build();
        when(chargeRepository.findById(any())).thenReturn(Optional.of(charge));
        when(adminClient.startExecutable(any())).thenReturn(any());
        IntermediateReportStatusRequest intermediateReportStatusRequest = new IntermediateReportStatusRequest()
                .chargeID(chargeID)
                .statusIntermediateReport(ChargeStatusEnum.DCM_APPLIED)
                .addMattersIntermediateReportItem(
                        new IntermediateReportMatterRequest()
                                .matterNr(matterNr)
                                .intermediateReportExplanation(IntermediateReportExplanationEnum.RELEASED));

        // Activate
        intermediateReportStatusService.processIntermediateReportStatusRequests(intermediateReportStatusRequest);

        // assert
        verify(chargeRepository, times(1)).findById(any());
        verify((adminClient), never()).startExecutable(any());
    }

    @Test
    void happyFlowEXECUTABLETest() {
        UUID chargeID = UUID.randomUUID();
        String matterNr = "12345";

        Charge charge = Charge.builder()
                .chargeStatus(ChargeStatusEnum.EXECUTABLE)
                .build();
        when(chargeRepository.findById(any())).thenReturn(Optional.of(charge));
        when(adminClient.startExecutable(any())).thenReturn(any());
        IntermediateReportStatusRequest intermediateReportStatusRequest = new IntermediateReportStatusRequest()
                .chargeID(chargeID)
                .statusIntermediateReport(ChargeStatusEnum.EXECUTABLE)
                .addMattersIntermediateReportItem(
                        new IntermediateReportMatterRequest()
                                .matterNr(matterNr)
                                .intermediateReportExplanation(IntermediateReportExplanationEnum.RELEASED));

        // Activate
        intermediateReportStatusService.processIntermediateReportStatusRequests(intermediateReportStatusRequest);

        // assert
        verify(chargeRepository, times(1)).findById(any());
        verify((adminClient), never()).startDCMApplied(any());
    }
}
