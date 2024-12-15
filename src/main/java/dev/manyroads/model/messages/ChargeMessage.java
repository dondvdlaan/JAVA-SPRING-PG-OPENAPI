package dev.manyroads.model.messages;

import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.enums.MatterStatus;

import java.util.UUID;

public record ChargeMessage(
        UUID chargeID,
        ChargeStatusEnum chargeStatus
) {
}
