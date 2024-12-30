package dev.manyroads.client;

import dev.manyroads.decomreception.exception.InternalException;
import dev.manyroads.matterreception.MatterReceptionService;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.messages.CustomerProcessingClientMessage;
import dev.manyroads.model.messages.MatterMessage;
import dev.manyroads.model.repository.ChargeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {dev.manyroads.DecomApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClientTests {

    @MockBean
    ChargeRepository chargeRepository;
    @Autowired
    CustomerProcessingClient customerProcessingClient;
    @Autowired
    MatterReceptionService matterReceptionService;

    @Test
    @DisplayName("Shall return InternalException started from MatterReceptionService")
    void restTemplateNotConnectedShallThrowExceptionStartedFromMatterReceptionTest() {
        // prepare
        long customerNr = (long) (Math.random() * 99999);
        UUID customerID = UUID.randomUUID();
        String matterNr = "121212";
        String terminationCallBackUrl = "mooi/wel";
        VehicleTypeEnum vehicleTypeEnum = VehicleTypeEnum.BULLDOZER;
        ChargeStatusEnum chargeStatus = ChargeStatusEnum.BOOKED;

        Customer existingCustomer = Customer.builder()
                .customerID(customerID)
                .customerNr(customerNr)
                .build();
        Charge existingCharge = new Charge();
        existingCharge.setChargeID(UUID.randomUUID());
        existingCharge.setChargeStatus(chargeStatus);
        existingCharge.setCustomerNr(customerNr);
        existingCharge.setVehicleType(VehicleTypeEnum.DIRTBIKE);
        existingCharge.setCustomer(existingCustomer);
        Matter existingMatter = Matter.builder()
                .matterNr(matterNr)
                .charge(existingCharge)
                .build();
        existingCharge.getMatters().add(existingMatter);

        String matterNr2 = "34343434";
        Charge existingCharge2 = new Charge();
        existingCharge.setChargeID(UUID.randomUUID());
        existingCharge.setChargeStatus(chargeStatus);
        existingCharge.setCustomerNr(customerNr);
        existingCharge.setVehicleType(VehicleTypeEnum.DIRTBIKE);
        existingCharge.setCustomer(existingCustomer);
        Matter existingMatter2 = Matter.builder()
                .matterNr(matterNr2)
                .charge(existingCharge2)
                .build();
        existingCharge2.getMatters().add(existingMatter2);
        List<Charge> listCharge = new ArrayList<>();
        listCharge.add(existingCharge);
        listCharge.add(existingCharge2);
        when(chargeRepository
                .findByCustomerNrAndChargeStatus(eq(customerNr), eq(chargeStatus)))
                .thenReturn(Optional.of(listCharge));

        // activate and verify
        assertThatThrownBy(() -> matterReceptionService.sendCustomerDataToCustomerProcessing(customerNr))
                .isInstanceOf(InternalException.class);
    }

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
