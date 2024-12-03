package dev.manyroads.intermediatereport;

import dev.manyroads.client.AdminClient;
import dev.manyroads.decomreception.exception.InternalException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusChargeIDNotExistException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusChargeTerminatedException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMattersNotBelongToChargeException;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.IntermediateReportMatterRequest;
import dev.manyroads.model.IntermediateReportStatusRequest;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.MatterRepository;
import dev.manyroads.utils.DCMutils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class IntermediateReportStatusService {

    AdminClient adminClient;
    ChargeRepository chargeRepository;
    MatterRepository matterRepository;

    public void processIntermediateReportStatusRequests(IntermediateReportStatusRequest intermediateReportStatusRequest) {
        log.info("processIntermediateReportStatusRequests: Start processing incoming IntermediateReportStatusRequest ");
        Charge charge = getCharge(intermediateReportStatusRequest);
        if (List.of(ChargeStatusEnum.DONE, ChargeStatusEnum.BOOKED, ChargeStatusEnum.REJECTED).contains(charge.getChargeStatus()))
            throw new IntermediateReportStatusChargeTerminatedException(
                    MessageFormat.format("DCM-306: Charge {0} terminated with state {1}.", charge.getChargeID(), charge.getChargeStatus())
            );

        switch (intermediateReportStatusRequest.getStatusIntermediateReport()) {
            case DCM_APPLIED -> adminClient.startDCMApplied(charge);
            case EXECUTABLE -> adminClient.startExecutable(charge);
            case PARTIALLY_EXECUTABLE ->
                    processPartiallyExecutable(charge, intermediateReportStatusRequest.getMattersIntermediateReport());
            default ->
                    throw new InternalException("processIntermediateReportStatusRequests: Default ChargeStatusEnum enums not matched ");
        }
    }

    private Charge getCharge(IntermediateReportStatusRequest intermediateReportStatusRequest) {
        Optional<Charge> ocharge = chargeRepository.findById(intermediateReportStatusRequest.getChargeID());
        ocharge.orElseThrow(() -> new IntermediateReportStatusChargeIDNotExistException(
                MessageFormat.format("DCM-305: ChargeID {0} does not exist.", intermediateReportStatusRequest.getChargeID().toString())));
        return ocharge.get();
    }

    private void processPartiallyExecutable(Charge charge, List<IntermediateReportMatterRequest> listMattersRequest) {
        // X-check matterNrs from request with charge and return list of unrelated matterNrs
        List<String> unrelatedMattersToCharge = DCMutils.mattersNotRelatedToCharge(charge, listMattersRequest);
        if (!unrelatedMattersToCharge.isEmpty())
            throw new IntermediateReportStatusMattersNotBelongToChargeException(
                    MessageFormat.format("DCM-306: Matters {0} do not belong to charge {1}.", unrelatedMattersToCharge, charge.getChargeID()));

        listMattersRequest.forEach(matterRequest -> {
            Optional<Matter> oMatter = matterRepository.findByMatterNrAndCharge(matterRequest.getMatterNr(), charge);
            oMatter.ifPresent(m -> {
                        m.setMatterStatus(MatterStatus.NON_EXECUTABLE);
                        m.setReasonTermination(matterRequest.getIntermediateReportExplanation());
                        matterRepository.save(m);
                    }
            );
        });
    }
}
