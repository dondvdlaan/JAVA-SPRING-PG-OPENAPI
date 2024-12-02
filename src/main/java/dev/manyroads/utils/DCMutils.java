package dev.manyroads.utils;

import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.entity.Charge;

import java.util.List;

public class DCMutils {

    static public List<Charge> isActive(List<Charge> listCharges) {
        return listCharges.stream().filter(c -> c.getChargeStatus() != ChargeStatusEnum.DONE_).toList();
    }

    static public boolean isBeingProcessed(Charge charge) {
        return charge.getChargeStatus() == ChargeStatusEnum.IN_PROCESS_;
    }
}
