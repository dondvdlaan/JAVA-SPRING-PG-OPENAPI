package dev.manyroads.decomreception;

import dev.manyroads.client.feign.AdminClient;
import dev.manyroads.client.CustomerProcessingClient;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterRequestCallback;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.messages.CustomerProcessingClientMessage;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.verification.Verification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DecomReceptionMatterRequestControllerITests {

    @LocalServerPort
    private int port;

    @Autowired
    DecomReceptionController decomReceptionController;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    Verification verification;
    @Autowired
    CustomerRepository customerRepository;
    @MockBean
    AdminClient adminClient;
    @MockBean
    CustomerProcessingClient customerProcessingClient;
    @Value("${customerStandByDuration}")
    Integer customerStandByDuration;


    @Test
    @DisplayName("Happy Flow MatterRequest with Customer StandBy")
    void matterRequestShouldReturnMatterResponseWithCustomerStandByTest() throws InterruptedException {
        // Prepare
        Long customerNr = (long) (Math.random() * 99999);
        String terminationCallBackUrl = "/v1/terminate-matter/";
        when(adminClient.searchVehicleType(any(String.class))).thenReturn(VehicleTypeEnum.BULLDOZER.toString());
        when(customerProcessingClient.sendMessageToCustomerProcessing(any(CustomerProcessingClientMessage.class))).thenReturn(true);

        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr("12345");
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl(terminationCallBackUrl);
        matterRequest.setCallback(matterRequestCallback);
        MatterResponse matterResponse = new MatterResponse();
        matterResponse.setCustomerNr(customerNr);
        Long expected = matterResponse.getCustomerNr();

        // Activate
        MatterResponse result = testRestTemplate.postForObject(
                "http://localhost:" + port + "/v1/matters",
                matterRequest,
                MatterResponse.class);

        // Verify
        verify(adminClient, times(1)).searchVehicleType(any(String.class));
        assertEquals(expected, result.getCustomerNr());

        // StandBy period
        Thread.sleep(customerStandByDuration * 1500);

        verify(customerProcessingClient, times(1)).sendMessageToCustomerProcessing(any(CustomerProcessingClientMessage.class));
    }
}
