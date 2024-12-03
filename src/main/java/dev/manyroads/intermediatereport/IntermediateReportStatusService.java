package dev.manyroads.intermediatereport;

import dev.manyroads.client.AdminClient;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusChargeIDNotExistException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusChargeTerminatedException;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.IntermediateReportStatusRequest;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.repository.ChargeRepository;
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

    public void processIntermediateReportStatusRequests(IntermediateReportStatusRequest intermediateReportStatusRequest) {
        log.info("processIntermediateReportStatusRequests: Start processing incoming IntermediateRportStatus ");
        Charge charge = getCharge(intermediateReportStatusRequest);
        if (List.of(ChargeStatusEnum.DONE, ChargeStatusEnum.BOOKED, ChargeStatusEnum.REJECTED).contains(charge.getChargeStatus()))
            throw new IntermediateReportStatusChargeTerminatedException(
                    MessageFormat.format("DCM-306: Charge {0} terminated with state {1}.", charge.getChargeID(), charge.getChargeStatus())
            );

        switch (intermediateReportStatusRequest.getStatusIntermediateReport()) {
            case DCM_APPLIED -> {
                adminClient.startDCMApplied(charge);
            }
            case EXECUTABLE -> {
            }
            case PARTIALLY_EXECUTABLE -> {
            }
        }

    }

    private Charge getCharge(IntermediateReportStatusRequest intermediateReportStatusRequest) {
        Optional<Charge> ocharge = chargeRepository.findById(intermediateReportStatusRequest.getChargeID());
        ocharge.orElseThrow(() -> new IntermediateReportStatusChargeIDNotExistException(
                MessageFormat.format("DCM-305: CharegID {0} does not exist.", intermediateReportStatusRequest.getChargeID().toString())));
        return ocharge.get();
    }
}
