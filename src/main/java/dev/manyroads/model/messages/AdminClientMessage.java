package dev.manyroads.model.messages;

import java.util.List;
import java.util.UUID;

public record AdminClientMessage(
        UUID chargeID,
        List<MatterMessage> listMatterMessage
) {
}
