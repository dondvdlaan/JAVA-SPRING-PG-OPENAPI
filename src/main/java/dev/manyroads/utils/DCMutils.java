package dev.manyroads.utils;

import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.IntermediateReportMatterRequest;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Matter;

import java.util.List;

public class DCMutils {

    static public List<Charge> isActive(List<Charge> listCharges) {
        return listCharges.stream().filter(c -> c.getChargeStatus() != ChargeStatusEnum.DONE).toList();
    }

    static public boolean isBeingProcessed(Charge charge) {
        return charge.getChargeStatus() == ChargeStatusEnum.DCM_APPLIED;
    }

    static public List<String> mattersNotRelatedToCharge(Charge charge, List<IntermediateReportMatterRequest> listMattersRequest) {
        List<String> listMatterNrsCharge = charge.getMatters().stream().map(Matter::getMatterNr).toList();
        return listMattersRequest.stream()
                .map(IntermediateReportMatterRequest::getMatterNr)
                .filter(matterNrRequest -> !listMatterNrsCharge.contains(matterNrRequest))
                .toList();
    }
}
