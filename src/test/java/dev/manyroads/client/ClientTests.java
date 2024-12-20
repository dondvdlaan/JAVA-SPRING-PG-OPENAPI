package dev.manyroads.client;

import dev.manyroads.decomreception.exception.InternalException;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.messages.CustomerProcessingClientMessage;
import dev.manyroads.model.messages.MatterMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = {dev.manyroads.DecomApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClientTests {

    @Autowired
    CustomerProcessingClient customerProcessingClient;

    @Test
    @DisplayName("Shall return InternalException")
    void restTemplateNotConnectedShallThrowException() {
        // prepare
        String matterNr = "12345";
        Matter matter = Matter.builder()
                .matterNr(matterNr)
                .matterStatus(MatterStatus.EXECUTABLE)
                .build();
        List<MatterMessage> listMatterMessage = List.of(matter.convertToMatterMessage());
        CustomerProcessingClientMessage customerProcessingClientMessage =
                new CustomerProcessingClientMessage(UUID.randomUUID(), listMatterMessage);

        // activate and verify
        assertThatThrownBy(() -> customerProcessingClient.sendMessageToCustomerProcessing(customerProcessingClientMessage))
                .isInstanceOf(InternalException.class);
    }
}
