package dev.manyroads.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import dev.manyroads.client.parent.ParentMicroserviceClient;
import dev.manyroads.model.OAuth2ResponseDTO;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.MatterStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = {dev.manyroads.DecomApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ParentClientHappyFlowTest {

    private final WireMockServer wireMockServer= new WireMockServer(new WireMockConfiguration().port(7090));;

    @Autowired
    private ParentMicroserviceClient parentMicroserviceClient;

    @BeforeEach
    void setup() throws InterruptedException {
        int port = 7090;
        //wireMockServer = new WireMockServer(new WireMockConfiguration().port(port));
        wireMockServer.start();
        System.out.printf("[@BeforeEach] wireMockServer started at port %d at %s: \n", port, LocalDateTime.now());
    }

    @AfterEach
    void teardown() throws InterruptedException {
        wireMockServer.resetAll();
        wireMockServer.stop();
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
        System.out.println("wireMockServer stopped at: " + LocalDateTime.now());
    }

    /**
     * This test is disabled, because it "leeks" to the next test "ParentClientRefreshTokenTest", although each individual test
     * runs fine
     *
     * @throws Exception
     */
    @Disabled
    @Test
    @DisplayName("Happy Flow Parent MicroService")
    void happyFlowParentMicroService() throws Exception {
        // prepare
        String APPLICATION_JSON = "application/json";
        ObjectMapper mapper = new ObjectMapper();
        String username = "decom";
        String password = "secret";
        String authorizationGrant = "BigGrant";
        String accessToken = "je hebt toegamg";
        //String accessToken = JWTUtilTest.getAuthenticationJWS(username);
        String vehicleType = "bulldozer";
        String matterNr = "12345";
        Matter matter = Matter.builder()
                .matterNr(matterNr)
                .matterStatus(MatterStatus.EXECUTABLE)
                .terminationCallBackUrl("/terminate")
                .build();
        OAuth2ResponseDTO responseDTO = new OAuth2ResponseDTO()
                .authorizationGrant(authorizationGrant)
                .redirectionURI("http://localhost:7090/access-token");
        OAuth2ResponseDTO responseDTO2 = new OAuth2ResponseDTO().vehicleType(VehicleTypeEnum.BULLDOZER);

        wireMockServer.stubFor(get(urlMatching("/auth"))
                .withHeader("Grant-Type", equalTo("Authorization Code"))
                .withBasicAuth(username, password)
                .willReturn(aResponse()
                        .withBody(mapper.writeValueAsString(responseDTO))
                        .withHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())));

        wireMockServer.stubFor(get(urlMatching("/access-token"))
                .withHeader("Grant-Type", equalTo("Authorization Code"))
                .withHeader("Authorization-Grant", equalTo(authorizationGrant))
                .willReturn(aResponse()
                        .withHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                        .withHeader("Access-Token", accessToken)
                        .withStatus(HttpStatus.OK.value())));

        wireMockServer.stubFor(post(urlMatching("/terminate"))
                .withHeader("Access-Token", equalTo(accessToken))
                .willReturn(aResponse()
                        .withHeader("content-type", APPLICATION_JSON)
                        .withStatus(200)));

        // activate
        boolean res = parentMicroserviceClient.requestParentMicroserviceToActivateTermination(matter);
        // Verify
        wireMockServer.verify(1, getRequestedFor(urlMatching("/auth")));
        wireMockServer.verify(1, getRequestedFor(urlMatching("/access-token")));
        wireMockServer.verify(1, postRequestedFor(urlMatching("/terminate")));
        assertTrue(res);
    }

}
