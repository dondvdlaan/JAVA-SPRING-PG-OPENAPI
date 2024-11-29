package dev.manyroads.execinterrup;

import dev.manyroads.execinterrup.exception.ChargeMissingForCustomerNrException;
import dev.manyroads.execinterrup.exception.MatterCustomerNrMismatchException;
import dev.manyroads.model.ExecInterrupEnum;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.ExecInterrupResponse;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.ChargeStatus;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.ExecInterrupRepository;
import dev.manyroads.model.repository.MatterRepository;
import org.junit.jupiter.api.BeforeEach;
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
    ExecInterrupRepository execInterrupRepository;

    @BeforeEach
    void setup() {
        chargeRepository = mock(ChargeRepository.class);
        matterRepository = mock(MatterRepository.class);
        execInterrupRepository = mock(ExecInterrupRepository.class);
        executionInterruptionService = new ExecutionInterruptionService(
                chargeRepository, matterRepository, execInterrupRepository);
    }

    @Test
    void matterAndCustomerIdMismatchThrowsExceptionTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        Long wrongCustomerNr = (long) (Math.random() * 99999);
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
        ExecInterrupRequest matterCustomerNrMismatchInterruptRequest = new ExecInterrupRequest();
        matterCustomerNrMismatchInterruptRequest.setCustomerNr(wrongCustomerNr);
        matterCustomerNrMismatchInterruptRequest.setExecInterrupType(ExecInterrupEnum.WITHDRAWN);
        matterCustomerNrMismatchInterruptRequest.setMatterID(matterId);
        when(matterRepository.findById(any())).thenReturn(Optional.of(existingMatter));
        ExecInterrupResponse expected = new ExecInterrupResponse();

        // activate - Verify
        assertThatThrownBy(() -> executionInterruptionService.processIncomingExecutionInterruptions(matterCustomerNrMismatchInterruptRequest))
                .isInstanceOf(MatterCustomerNrMismatchException.class)
                .hasMessage(String.format("DCM-208: ExecInterrup Matter with id %s not found for CustomerNr: %d",
                        existingMatter.getMatterID(), matterCustomerNrMismatchInterruptRequest.getCustomerNr()));
        verify(matterRepository, times(1)).findById(any());
        verify(execInterrupRepository, times(1)).save(any());
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

        // Verify
        verify(execInterrupRepository, times(1)).save(any());
        verify(matterRepository, times(3)).findById(any());
        verify(matterRepository, times(1)).save(any());
        oMatter.ifPresent(m -> assertEquals(MatterStatus.WITHDRAWN, m.getMatterStatus()));
        assertEquals(expected, result);
    }

    @Test
    void noChargeForCustomerNrShallThrowChargeMissingForCustomerNrExceptionTest() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        ExecInterrupRequest noChargeForCustomerInterruptRequest = new ExecInterrupRequest();
        noChargeForCustomerInterruptRequest.setCustomerNr(customerNr);
        noChargeForCustomerInterruptRequest.setExecInterrupType(ExecInterrupEnum.CUSTOMER_DECEASED);
        noChargeForCustomerInterruptRequest.setMatterID(null);

        when(chargeRepository.findByCustomerNr(anyLong())).thenReturn(null);

        // Activate - Verify
        assertThatThrownBy(() -> executionInterruptionService.processIncomingExecutionInterruptions(noChargeForCustomerInterruptRequest))
                .isInstanceOf(ChargeMissingForCustomerNrException.class)
                .hasMessage(String.format("DCM-205: ExecInterrup No Charge found for CustomerNr: %d", customerNr));
        verify(chargeRepository, times(1)).findByCustomerNr(anyLong());
        verify(execInterrupRepository, times(1)).save(any());
        verify(chargeRepository, times(0)).save(any());
    }

    @Test
    void customerNrCorrectMatterIdEmptyShallReturnExecInterrupResponseNotNull() {
        // prepare
        Long customerNr = (long) (Math.random() * 99999);
        ExecInterrupRequest happyCustomerInterruptRequest =
                new ExecInterrupRequest()
                        .customerNr(customerNr)
                        .execInterrupType(ExecInterrupEnum.CUSTOMER_DECEASED)
                        .matterID(null);

        Charge existingCharge = new Charge();
        existingCharge.setChargeStatus(ChargeStatus.BOOKED);
        existingCharge.setCustomerNr(customerNr);
        List<Charge> listCharges = (List.of(existingCharge));
        when(chargeRepository.findByCustomerNr(anyLong())).thenReturn(Optional.of(listCharges));
        ExecInterrupResponse expected = new ExecInterrupResponse();

        // activate
        ExecInterrupResponse result = executionInterruptionService.processIncomingExecutionInterruptions(happyCustomerInterruptRequest);
        Optional<List<Charge>> oListCharge = chargeRepository.findByCustomerNr(customerNr);

        // Verify
        verify(chargeRepository, times(2)).findByCustomerNr(anyLong());
        verify(execInterrupRepository, times(1)).save(any());
        verify(chargeRepository, times(1)).save(any());
        oListCharge.ifPresent(cl -> cl.forEach(
                c -> assertEquals(ChargeStatus.CUSTOMER_DECEASED, c.getChargeStatus())));
        assertEquals(expected, result);
    }
}
