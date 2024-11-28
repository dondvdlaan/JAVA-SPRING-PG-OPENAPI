package dev.manyroads.execinterrup;

import dev.manyroads.execinterrup.exception.ChargeMissingForCustomerNrException;
import dev.manyroads.execinterrup.exception.CustomerNrIsMissingException;
import dev.manyroads.model.ExecInterrupEnum;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.ExecInterrupResponse;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.ChargeStatus;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.MatterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExecutionInterruptionServiceTest {

    ExecutionInterruptionService executionInterruptionService;
    ChargeRepository chargeRepository;
    MatterRepository matterRepository;

    @BeforeEach
    void setup() {
        chargeRepository = mock(ChargeRepository.class);
        matterRepository = mock(MatterRepository.class);
        executionInterruptionService = new ExecutionInterruptionService(chargeRepository, matterRepository);
    }

    @Test
    void happyFlowMatterWithdrawnTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        UUID chargeId = UUID.randomUUID();
        String matterId = UUID.randomUUID().toString();

        Charge existingCharge = new Charge();
        existingCharge.setChargeID(chargeId);
        existingCharge.setChargeStatus(ChargeStatus.BOOKED);
        existingCharge.setCustomerNr(customerNr);
        Matter existingMatter = new Matter();
        existingMatter.setMatterID(UUID.fromString(matterId));
        existingMatter.setMatterStatus(MatterStatus.EXECUTABLE);
        existingMatter.setCustomerNr(customerNr);
        existingMatter.setCharge(existingCharge);
        ExecInterrupRequest happyCustomerInterruptRequest = new ExecInterrupRequest();
        happyCustomerInterruptRequest.setCustomerNr(customerNr);
        happyCustomerInterruptRequest.setExecInterrupType(ExecInterrupEnum.WITHDRAWN);
        happyCustomerInterruptRequest.setMatterID(matterId);
        when(matterRepository.findById(any())).thenReturn(Optional.of(existingMatter));
        ExecInterrupResponse expected = new ExecInterrupResponse();

        // activate
        ExecInterrupResponse result = executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest);
        Optional<Matter> oMatter = matterRepository.findById(UUID.fromString(matterId));
        oMatter.ifPresent(System.out::println);
        
        // Verify
        verify(matterRepository, times(2)).findById(any());
        /*
        assertThatThrownBy(() -> executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest))
                .isInstanceOf(ChargeMissingForCustomerNrException.class)
                .hasMessage(String.format("DCM-205: ExecInterrup No Charge found for CustomerNr: %d", customerNr));
         */
        oMatter.ifPresent(m -> assertEquals(MatterStatus.WITHDRAWN, m.getMatterStatus()));
        assertEquals(expected, result);
    }

    @Test
    void noChargeForCustomerNrShallThrowChargeMissingForCustomerNrExceptionTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        ExecInterrupRequest happyCustomerInterruptRequest = new ExecInterrupRequest();
        happyCustomerInterruptRequest.setCustomerNr(customerNr);
        happyCustomerInterruptRequest.setExecInterrupType(ExecInterrupEnum.CUSTOMER_DECEASED);
        happyCustomerInterruptRequest.setMatterID(null);
        Charge existingCharge = new Charge();
        existingCharge.setChargeStatus(ChargeStatus.BOOKED);
        existingCharge.setCustomerNr(customerNr);
        Optional<List<Charge>> listCharges = Optional.of(List.of(existingCharge));
        when(chargeRepository.findByCustomerNr(anyLong())).thenReturn(null);
        ExecInterrupResponse expected = new ExecInterrupResponse();

        // Activate - Verify
        assertThatThrownBy(() -> executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest))
                .isInstanceOf(ChargeMissingForCustomerNrException.class)
                .hasMessage(String.format("DCM-205: ExecInterrup No Charge found for CustomerNr: %d", customerNr));
        verify(chargeRepository, times(1)).findByCustomerNr(anyLong());
        verify(chargeRepository, times(0)).save(any());
    }

    @Test
    void customerNrCorrectMatterIdEmptyShallReturnExecInterrupResponseNotNull() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        ExecInterrupRequest happyCustomerInterruptRequest = new ExecInterrupRequest();
        happyCustomerInterruptRequest.setCustomerNr(customerNr);
        happyCustomerInterruptRequest.setExecInterrupType(ExecInterrupEnum.CUSTOMER_DECEASED);
        happyCustomerInterruptRequest.setMatterID(null);
        Charge existingCharge = new Charge();
        existingCharge.setChargeStatus(ChargeStatus.BOOKED);
        existingCharge.setCustomerNr(customerNr);
        List<Charge> listCharges = (List.of(existingCharge));
        when(chargeRepository.findByCustomerNr(anyLong())).thenReturn(listCharges);
        ExecInterrupResponse expected = new ExecInterrupResponse();

        // activate
        ExecInterrupResponse result = executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest);
        List<Charge> listCharge = chargeRepository.findByCustomerNr(customerNr);

        // Verify
        verify(chargeRepository, times(2)).findByCustomerNr(anyLong());
        verify(chargeRepository, times(1)).save(any());
        listCharge.forEach(c -> assertEquals(ChargeStatus.CUSTOMER_DECEASED, c.getChargeStatus()));
        assertEquals(expected, result);
    }
}
