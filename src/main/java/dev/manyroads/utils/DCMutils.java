package dev.manyroads.utils;

import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.enums.ChargeStatus;

import java.util.List;

public class DCMutils {

    static public List<Charge> isActive(List<Charge> listCharges){
        return listCharges.stream().filter(c->c.getChargeStatus()!= ChargeStatus.DONE).toList();
    }
}
