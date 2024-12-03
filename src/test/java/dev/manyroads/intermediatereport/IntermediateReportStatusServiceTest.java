package dev.manyroads.intermediatereport;

import dev.manyroads.client.AdminClient;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
        when(matterRepository.findByMatterNrAndCharge(eq(matterNr),eq(charge))).thenReturn(Optional.of(matter));
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
