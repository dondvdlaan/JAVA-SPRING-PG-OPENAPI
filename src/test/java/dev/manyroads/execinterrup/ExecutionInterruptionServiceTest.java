package dev.manyroads.execinterrup;

import dev.manyroads.execinterrup.exception.ChargeMissingForCustomerNrException;
import dev.manyroads.execinterrup.exception.CustomerNrIsMissingException;
import dev.manyroads.model.ExecInterrupEnum;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.ExecInterrupResponse;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.enums.ChargeStatus;
import dev.manyroads.model.repository.ChargeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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

    @BeforeEach
    void setup() {
        chargeRepository = mock(ChargeRepository.class);
        executionInterruptionService = new ExecutionInterruptionService(chargeRepository);
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

        // activate
        //ExecInterrupResponse result = executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest);

        // Verify
        verify(chargeRepository, times(0)).findByCustomerNr(anyLong());
        verify(chargeRepository, times(0)).save(any());
        assertThatThrownBy(() -> executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest))
                .isInstanceOf(ChargeMissingForCustomerNrException.class)
                .hasMessage(String.format("DCM-205: ExecInterrup No Charge found for CustomerNr: %d", customerNr));
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
