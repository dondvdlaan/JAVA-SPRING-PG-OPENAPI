package dev.manyroads.model.messages;

import dev.manyroads.model.enums.MatterStatus;

public record MatterMessage(
        String matterNr,
        MatterStatus matterStatus
) {
}
