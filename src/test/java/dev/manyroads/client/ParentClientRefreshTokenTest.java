package dev.manyroads.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import dev.manyroads.client.parent.ParentMicroserviceClient;
import dev.manyroads.model.OAuth2ResponseDTO;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.MatterStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest(
        classes = {dev.manyroads.DecomApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ParentClientRefreshTokenTest {

    WireMockServer wireMockServer;

    @Autowired
    ParentMicroserviceClient parentMicroserviceClient;

    @BeforeEach
    void setup() throws InterruptedException  {
        int port = 7090;
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(port));
        wireMockServer.start();
        WireMock.configureFor("localhost", port);
        System.out.printf("[@BeforeEach] wireMockServer started at port %d at %s: \n", port, LocalDateTime.now());
    }

    @AfterEach
    void teardown() {
        wireMockServer.stop();
        wireMockServer.resetAll();
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
        System.out.println("wireMockServer stopped at: " + LocalDateTime.now());
    }

    @Test
    @DisplayName("Refreshing accessCode Parent MicroService")
    void refreshingAccessCodeParentMicroService() throws Exception {
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

        stubFor(get(urlMatching("/auth"))
                .withHeader("Grant-Type", equalTo("Authorization Code"))
                .withBasicAuth(username, password)
                .willReturn(aResponse()
                        .withBody(mapper.writeValueAsString(responseDTO))
                        .withHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())));

        stubFor(get(urlMatching("/access-token"))
                .withHeader("Grant-Type", equalTo("Authorization Code"))
                .withHeader("Authorization-Grant", equalTo(authorizationGrant))
                .willReturn(aResponse()
                        .withHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                        .withHeader("Access-Token", accessToken)
                        .withStatus(HttpStatus.OK.value())));

        // accesscode expired
        stubFor(post(urlMatching("/terminate"))
                .withHeader("Access-Token", equalTo(accessToken))
                .inScenario("Refresh Scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse()
                        .withBody(mapper.writeValueAsString(new OAuth2ResponseDTO()))
                        .withHeader("content-type", APPLICATION_JSON)
                        .withStatus(HttpStatus.UNAUTHORIZED.value()))
                .willSetStateTo("Refresh"));

        // accesscode refreshed
        stubFor(post(urlMatching("/terminate"))
                .withHeader("Access-Token", equalTo(accessToken))
                .inScenario("Refresh Scenario")
                .whenScenarioStateIs("Refresh")
                .willReturn(aResponse()
                        .withBody(mapper.writeValueAsString(new OAuth2ResponseDTO()))
                        .withHeader("content-type", APPLICATION_JSON)
                        .withStatus(HttpStatus.OK.value())));

        // activate
        boolean res = parentMicroserviceClient.requestParentMicroserviceToActivateTermination(matter);
        // Verify
        verify(2, getRequestedFor(urlMatching("/auth")));
        verify(2, getRequestedFor(urlMatching("/access-token")));
        verify(2, postRequestedFor(urlMatching("/terminate")));
        assertTrue(res);
    }
}
