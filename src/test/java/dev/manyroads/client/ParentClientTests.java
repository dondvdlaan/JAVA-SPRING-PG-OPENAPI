package dev.manyroads.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dev.manyroads.client.parent.ParentMicroserviceClient;
import dev.manyroads.decomreception.exception.InternalException;
import dev.manyroads.matterreception.MatterReceptionService;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.messages.CustomerProcessingClientMessage;
import dev.manyroads.model.messages.MatterMessage;
import dev.manyroads.model.repository.ChargeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(
        classes = {dev.manyroads.DecomApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ParentClientTests {

    private static WireMockServer wireMockServer;
    static int port;
    private static final String APPLICATION_JSON = "application/json";
    @MockitoBean
    ChargeRepository chargeRepository;
    @Autowired
    ParentMicroserviceClient parentMicroserviceClient;
    @Autowired
    CustomerProcessingClient customerProcessingClient;
    @Autowired
    MatterReceptionService matterReceptionService;

    @BeforeAll
    static void setup() {
        port = 7091;
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(7091));
        wireMockServer.start();
        WireMock.configureFor("localhost", port);
        System.out.printf("[@BeforeEach] wireMockServer started at port %d at %s: \n", port, LocalDateTime.now());
    }

    @AfterEach
    void teardown() throws InterruptedException {
        //Thread.sleep(1000000);
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
        System.out.println("wireMockServer stopped at: " + LocalDateTime.now());
    }
    @Test
    @DisplayName("Happy Flow Parent MicroService")
    void happyFlowParentMicroService() {
        // prepare
        String matterNr = "12345";
        Matter matter = Matter.builder()
                .matterNr(matterNr)
                .matterStatus(MatterStatus.EXECUTABLE)
                .terminationCallBackUrl("/terminate")
                .build();
        stubFor(post(urlMatching("/terminate"))
                .willReturn(aResponse()
                        .withHeader("content-type", APPLICATION_JSON)
                        .withStatus(200)));

        // activate
        boolean res = parentMicroserviceClient.requestParentMicroserviceToActivateTermination(matter);
        // Verify
        verify(1, postRequestedFor(urlMatching("/terminate")));
        assertTrue(res);

    }

    @Disabled
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
