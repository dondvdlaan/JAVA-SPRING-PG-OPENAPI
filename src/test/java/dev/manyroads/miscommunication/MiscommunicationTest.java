package dev.manyroads.miscommunication;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dev.manyroads.matterreception.MatterReceptionService;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterRequestCallback;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This test setup is not working well!!
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWireMock(port = 7090)
public class MiscommunicationTest {
    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int port;
    @Autowired
    MatterReceptionService matterReceptionService;
    //@MockitoBean
    //AdminClient adminClient;
    @MockitoBean
    CustomerRepository customerRepository;
    @MockitoBean
    ChargeRepository chargeRepository;
    /*
   @MockitoBean
    MatterRepository matterRepository;
    @MockitoBean
    CustomerProcessingClient customerProcessingClient;
    @MockitoBean
    SchedulerService schedulerService;
*/


    @BeforeEach
    void setup() {
        int port = 7090;
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(7090));
        wireMockServer.start();
        WireMock.configureFor("localhost", port);
        System.out.printf("[@BeforeEach] wireMockServer started at port %d at %s: \n", port, LocalDateTime.now());
    }


    @AfterEach
    void teardown() throws InterruptedException {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
        //System.out.println("wireMockServer stopped at: " + LocalDateTime.now());
    }
/*
    @BeforeEach
    void setUp() {
        this.adminClient = mock(AdminClient.class);
        this.customerRepository = mock(CustomerRepository.class);
        this.chargeRepository = mock(ChargeRepository.class);
        this.matterRepository = mock(MatterRepository.class);
        this.customerProcessingClient = mock(CustomerProcessingClient.class);
        this.schedulerService = mock(SchedulerService.class);
        this.matterReceptionService = new MatterReceptionService(
                adminClient, customerRepository, chargeRepository, matterRepository, customerProcessingClient, schedulerService);
    }

 */

    @Disabled
    @Test
    void happyFlowCustomerProcessing() {
        // prepare
        wireMockServer.stubFor(post(urlEqualTo(("/v1/process_charge")))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));


        Long customerNr = (long) (Math.random() * 99999);
        UUID customerID = UUID.randomUUID();
        String matterNr = "121212";
        VehicleTypeEnum existingVehicle = VehicleTypeEnum.DIRTBIKE;
        VehicleTypeEnum requestedVehicle = VehicleTypeEnum.BULLDOZER;
        Customer existingCustomer = new Customer();
        existingCustomer.setCustomerID(customerID);
        existingCustomer.setCustomerNr(customerNr);
        existingCustomer.setStandByFlag(true);
        when(customerRepository.findByCustomerNr(anyLong())).thenReturn(existingCustomer);
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);
        Charge existingCharge = new Charge();
        existingCharge.setChargeID(UUID.randomUUID());
        existingCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        existingCharge.setCustomerNr(customerNr);
        existingCharge.setVehicleType(existingVehicle);
        existingCharge.setCustomer(existingCustomer);
        List<Charge> listCharge = new ArrayList<>();
        listCharge.add(existingCharge);
        when(chargeRepository.findByCustomerNrAndChargeStatus(anyLong(), any(ChargeStatusEnum.class))).thenReturn(Optional.of(listCharge));

        // activate
        matterReceptionService.sendCustomerDataToCustomerProcessing(customerNr);

        // verify
        verify(chargeRepository, times(1)).findByCustomerNrAndChargeStatus(anyLong(), any(ChargeStatusEnum.class));
        verify(customerRepository, times(1)).findByCustomerNr(anyLong());
    }

    @Test
    void status500CustomerProcessing() {
        // prepare
        wireMockServer.stubFor(post(urlEqualTo(("/v1/process_charge")))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(500)));


        Long customerNr = (long) (Math.random() * 99999);
        UUID customerID = UUID.randomUUID();
        String matterNr = "121212";
        VehicleTypeEnum existingVehicle = VehicleTypeEnum.DIRTBIKE;
        VehicleTypeEnum requestedVehicle = VehicleTypeEnum.BULLDOZER;
        Customer existingCustomer = new Customer();
        existingCustomer.setCustomerID(customerID);
        existingCustomer.setCustomerNr(customerNr);
        existingCustomer.setStandByFlag(true);
        when(customerRepository.findByCustomerNr(anyLong())).thenReturn(existingCustomer);
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MatterRequestCallback matterRequestCallback = new MatterRequestCallback();
        matterRequestCallback.setTerminationCallBackUrl("mooi/wel");
        matterRequest.setCallback(matterRequestCallback);
        Charge existingCharge = new Charge();
        existingCharge.setChargeID(UUID.randomUUID());
        existingCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        existingCharge.setCustomerNr(customerNr);
        existingCharge.setVehicleType(existingVehicle);
        existingCharge.setCustomer(existingCustomer);
        List<Charge> listCharge = new ArrayList<>();
        listCharge.add(existingCharge);
        when(chargeRepository.findByCustomerNrAndChargeStatus(anyLong(), any(ChargeStatusEnum.class))).thenReturn(Optional.of(listCharge));

        // activate
        matterReceptionService.sendCustomerDataToCustomerProcessing(customerNr);

        // verify
        verify(chargeRepository, times(1)).findByCustomerNrAndChargeStatus(anyLong(), any(ChargeStatusEnum.class));
        verify(customerRepository, times(1)).findByCustomerNr(anyLong());
    }


}
