package dev.manyroads.execinterrup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dev.manyroads.client.AdminClient;
import dev.manyroads.client.parent.ParentMicroserviceClient;
import dev.manyroads.execinterrup.exception.ChargeMissingForCustomerNrException;
import dev.manyroads.execinterrup.exception.MatterCustomerNrMismatchException;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.ExecInterrupEnum;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.ExecInterrupResponse;
import dev.manyroads.model.OAuth2ResponseDTO;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.ExecInterrupRepository;
import dev.manyroads.model.repository.MatterRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {dev.manyroads.DecomApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Slf4j
public class ExecutionInterruptionServiceParentClientTest {

    private static final String APPLICATION_JSON = "application/json";
    private static WireMockServer wireMockServer;
    static int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ExecutionInterruptionService executionInterruptionService;
    @MockitoBean
    ChargeRepository chargeRepository;
    @MockitoBean
    MatterRepository matterRepository;
    @MockitoBean
    ExecInterrupRepository execInterrupRepository;
    @MockitoBean
    AdminClient adminClient;
    @Autowired
    ParentMicroserviceClient parentMicroserviceClient;

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
    void happyFlowCustomerDeceasedTest() throws Exception {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        UUID chargeId = UUID.randomUUID();
        String matterId = UUID.randomUUID().toString();
        String matterNr = "147852";

        Charge existingCharge = new Charge();
        existingCharge.setChargeID(chargeId);
        existingCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        existingCharge.setCustomerNr(customerNr);

        Matter existingMatter = new Matter();
        existingMatter.setMatterID(UUID.fromString(matterId));
        existingMatter.setMatterStatus(MatterStatus.EXECUTABLE);
        //existingMatter.setCharge(existingCharge);

        existingCharge.getMatters().add(existingMatter);
        List<Charge> listCharges = List.of(existingCharge);

        ExecInterrupRequest happyCustomerInterruptRequest = new ExecInterrupRequest()
                .customerNr(customerNr)
                .execInterrupType(ExecInterrupEnum.CUSTOMER_DECEASED);

        when(chargeRepository.findByCustomerNr(anyLong())).thenReturn(Optional.of(listCharges));
        // prepare
        ObjectMapper mapper = new ObjectMapper();
        String username = "decom";
        String password = "secret";
        String authorizationGrant = "BigGrant";
        String accessToken = "je hebt toegamg";
        //String accessToken = JWTUtilTest.getAuthenticationJWS(username);
        String vehicleType = "bulldozer";
        //String matterNr = "12345";
        Matter matter = Matter.builder()
                .matterNr(matterNr)
                .matterStatus(MatterStatus.EXECUTABLE)
                .terminationCallBackUrl("/terminate")
                .build();
        OAuth2ResponseDTO responseDTO = new OAuth2ResponseDTO()
                .authorizationGrant(authorizationGrant)
                .redirectionURI("http://localhost:7090/access-token");
        OAuth2ResponseDTO responseDTO2 = new OAuth2ResponseDTO().vehicleType(VehicleTypeEnum.valueOf(vehicleType));
        log.info("smokeTest(): mapper -> " + mapper.writeValueAsString(responseDTO));

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

        stubFor(post(urlMatching("/terminate"))
                .willReturn(aResponse()
                        .withHeader("content-type", APPLICATION_JSON)
                        .withStatus(200)));

       // when(parentMicroserviceClient.requestParentMicroserviceToActivateTermination(eq(existingMatter))).thenReturn(true);

        ExecInterrupResponse expected = new ExecInterrupResponse();

        // activate
        //ExecInterrupResponse result = executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest);
        MvcResult res = mockMvc.perform(MockMvcRequestBuilders.get("/smoke"))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();
        // Verify
        verify(execInterrupRepository, times(1)).save(any());
        verify(chargeRepository, times(1)).findByCustomerNr(anyLong());
        verify(adminClient, times(1)).terminateMatter(any());
        verify(parentMicroserviceClient, times(1)).requestParentMicroserviceToActivateTermination(any());
    }

    @Test
    void CustomerPaidButChargeInRejectPhaseTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        UUID chargeId = UUID.randomUUID();
        String matterId = UUID.randomUUID().toString();
        String matterNr = "147852";

        Charge existingCharge = new Charge();
        existingCharge.setChargeID(chargeId);
        existingCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        existingCharge.setCustomerNr(customerNr);

        Matter existingMatter = new Matter();
        existingMatter.setMatterID(UUID.fromString(matterId));
        existingMatter.setMatterStatus(MatterStatus.EXECUTABLE);
        existingMatter.setMatterNr(matterNr);
        existingMatter.setCharge(existingCharge);

        //existingCharge.getMatters().add(existingMatter);
        List<Charge> listCharges = List.of(existingCharge);

        ExecInterrupRequest happyCustomerInterruptRequest = new ExecInterrupRequest()
                .customerNr(customerNr)
                .execInterrupType(ExecInterrupEnum.PAID)
                .matterNr(matterId);

        when(matterRepository.findById(any())).thenReturn(Optional.of(existingMatter));

        // activate
        ExecInterrupResponse result = executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest);

        // Verify
        verify(execInterrupRepository, times(1)).save(any());
        verify(matterRepository, times(2)).findById(any());
        verify(adminClient, never()).terminateMatter(any());
        verify(parentMicroserviceClient, never()).requestParentMicroserviceToActivateTermination(any());

    }

    @Test
    void happyFlowCustomerDPaidTest() throws Exception {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        UUID chargeId = UUID.randomUUID();
        String matterId = UUID.randomUUID().toString();
        String matterNr = "147852";

        Charge existingCharge = new Charge();
        existingCharge.setChargeID(chargeId);
        existingCharge.setChargeStatus(ChargeStatusEnum.DCM_APPLIED);
        existingCharge.setCustomerNr(customerNr);

        Matter existingMatter = new Matter();
        existingMatter.setMatterID(UUID.fromString(matterId));
        existingMatter.setMatterStatus(MatterStatus.EXECUTABLE);
        existingMatter.setMatterNr(matterNr);
        existingMatter.setCharge(existingCharge);

        //existingCharge.getMatters().add(existingMatter);
        List<Charge> listCharges = List.of(existingCharge);

        ExecInterrupRequest happyCustomerInterruptRequest = new ExecInterrupRequest()
                .customerNr(customerNr)
                .execInterrupType(ExecInterrupEnum.PAID)
                .matterNr(matterId);

        when(matterRepository.findById(any())).thenReturn(Optional.of(existingMatter));

        // activate
        ExecInterrupResponse result = executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest);

        // Verify
        verify(execInterrupRepository, times(1)).save(any());
        verify(matterRepository, times(2)).findById(any());
        verify(adminClient, times(1)).terminateMatter(any());
        verify(parentMicroserviceClient, never()).requestParentMicroserviceToActivateTermination(any());

    }

    @Test
    void matterAndCustomerIdMismatchThrowsExceptionTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        Long wrongCustomerNr = (long) (Math.random() * 99999);
        UUID chargeId = UUID.randomUUID();
        String matterId = UUID.randomUUID().toString();
        String matterNr = "147852";

        Charge existingCharge = new Charge();
        existingCharge.setChargeID(chargeId);
        existingCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        existingCharge.setCustomerNr(customerNr);
        Matter existingMatter = new Matter();
        existingMatter.setMatterID(UUID.fromString(matterId));
        existingMatter.setMatterStatus(MatterStatus.EXECUTABLE);
        existingMatter.setMatterNr(matterNr);
        existingMatter.setCharge(existingCharge);
        ExecInterrupRequest matterCustomerNrMismatchInterruptRequest = new ExecInterrupRequest();
        matterCustomerNrMismatchInterruptRequest.setCustomerNr(wrongCustomerNr);
        matterCustomerNrMismatchInterruptRequest.setExecInterrupType(ExecInterrupEnum.WITHDRAWN);
        matterCustomerNrMismatchInterruptRequest.setMatterNr(matterId);
        when(matterRepository.findById(any())).thenReturn(Optional.of(existingMatter));
        ExecInterrupResponse expected = new ExecInterrupResponse();

        // activate - Verify
        assertThatThrownBy(() -> executionInterruptionService.processIncomingExecutionInterruptions(matterCustomerNrMismatchInterruptRequest))
                .isInstanceOf(MatterCustomerNrMismatchException.class)
                .hasMessage(String.format("DCM-208: ExecInterrup Matter with id %s not found for CustomerNr: %d",
                        existingMatter.getMatterID(), matterCustomerNrMismatchInterruptRequest.getCustomerNr()));
        verify(matterRepository, times(1)).findById(any());
        verify(execInterrupRepository, times(1)).save(any());
        verify(adminClient, never()).terminateMatter(any());
        verify(parentMicroserviceClient, never()).requestParentMicroserviceToActivateTermination(any());
    }

    @Test
    void happyFlowMatterWithdrawnTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        UUID chargeId = UUID.randomUUID();
        String matterId = UUID.randomUUID().toString();
        String matterNr = "147852";

        Charge existingCharge = new Charge();
        existingCharge.setChargeID(chargeId);
        existingCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        existingCharge.setCustomerNr(customerNr);
        Matter existingMatter = new Matter();
        existingMatter.setMatterID(UUID.fromString(matterId));
        existingMatter.setMatterStatus(MatterStatus.EXECUTABLE);
        existingMatter.setMatterNr(matterNr);
        existingMatter.setCharge(existingCharge);
        ExecInterrupRequest happyCustomerInterruptRequest = new ExecInterrupRequest();
        happyCustomerInterruptRequest.setCustomerNr(customerNr);
        happyCustomerInterruptRequest.setExecInterrupType(ExecInterrupEnum.WITHDRAWN);
        happyCustomerInterruptRequest.setMatterNr(matterId);
        when(matterRepository.findById(any())).thenReturn(Optional.of(existingMatter));
        ExecInterrupResponse expected = new ExecInterrupResponse();

        // activate
        ExecInterrupResponse result = executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest);
        Optional<Matter> oMatter = matterRepository.findById(UUID.fromString(matterId));

        // Verify
        verify(execInterrupRepository, times(1)).save(any());
        verify(matterRepository, times(3)).findById(any());
        verify(matterRepository, times(1)).save(any());
        oMatter.ifPresent(m -> assertEquals(MatterStatus.WITHDRAWN, m.getMatterStatus()));
        assertEquals(expected, result);
        verify(adminClient, never()).terminateMatter(any());
        verify(parentMicroserviceClient, never()).requestParentMicroserviceToActivateTermination(any());

    }

    @Test
    void noChargeForCustomerNrShallThrowChargeMissingForCustomerNrExceptionTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        ExecInterrupRequest noChargeForCustomerInterruptRequest = new ExecInterrupRequest();
        noChargeForCustomerInterruptRequest.setCustomerNr(customerNr);
        noChargeForCustomerInterruptRequest.setExecInterrupType(ExecInterrupEnum.CUSTOMER_DECEASED);
        noChargeForCustomerInterruptRequest.setMatterNr(null);

        when(chargeRepository.findByCustomerNr(anyLong())).thenReturn(Optional.empty());

        // Activate - Verify
        assertThatThrownBy(() -> executionInterruptionService.processIncomingExecutionInterruptions(noChargeForCustomerInterruptRequest))
                .isInstanceOf(ChargeMissingForCustomerNrException.class)
                .hasMessage(String.format("DCM-205: ExecInterrup No Charge found for CustomerNr: %d", customerNr));
        verify(chargeRepository, times(1)).findByCustomerNr(anyLong());
        verify(execInterrupRepository, times(1)).save(any());
        verify(chargeRepository, times(0)).save(any());
        verify(adminClient, never()).terminateMatter(any());
        verify(parentMicroserviceClient, never()).requestParentMicroserviceToActivateTermination(any());
    }

    @Test
    @DisplayName("Charge without Matters")
    void customerNrCorrectMatterNrNullShallReturnExecInterrupResponseNotNull() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        ExecInterrupRequest happyCustomerInterruptRequest =
                new ExecInterrupRequest()
                        .customerNr(customerNr)
                        .execInterrupType(ExecInterrupEnum.CUSTOMER_DECEASED)
                        .matterNr(null);

        Charge existingCharge = new Charge();
        existingCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        existingCharge.setCustomerNr(customerNr);
        List<Charge> listCharges = (List.of(existingCharge));
        when(chargeRepository.findByCustomerNr(anyLong())).thenReturn(Optional.of(listCharges));
        ExecInterrupResponse expected = new ExecInterrupResponse(customerNr);

        // activate
        ExecInterrupResponse result = executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest);
        Optional<List<Charge>> oListCharge = chargeRepository.findByCustomerNr(customerNr);

        // Verify
        verify(chargeRepository, times(2)).findByCustomerNr(anyLong());
        verify(execInterrupRepository, times(1)).save(any());
        verify(chargeRepository, times(1)).save(any());
        verify(adminClient, never()).terminateMatter(any());
        verify(parentMicroserviceClient, never()).requestParentMicroserviceToActivateTermination(any());
        oListCharge.ifPresent(cl -> cl.forEach(
                c -> assertEquals(ChargeStatusEnum.CUSTOMER_DECEASED, c.getChargeStatus())));
        assertEquals(expected, result);
    }
}
