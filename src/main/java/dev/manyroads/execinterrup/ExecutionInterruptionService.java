package dev.manyroads.execinterrup;

import dev.manyroads.decomreception.exception.InternalException;
import dev.manyroads.execinterrup.exception.ChargeMissingForCustomerNrException;
import dev.manyroads.model.ExecInterrupEnum;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.ExecInterrupResponse;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.enums.ChargeStatus;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.repository.ChargeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
@Slf4j
public class ExecutionInterruptionService {

    ChargeRepository chargeRepository;

    public ExecInterrupResponse processIncomingExecutionInterruptions(ExecInterrupRequest execInterrupRequest) {
        log.info("Processing of Execution Interruption for customer nr: {} started.", execInterrupRequest.getCustomerNr());
        ExecInterrupResponse execInterrupResponse = null;

        if (!execInterrupRequest.getCustomerNr().toString().isEmpty() && (execInterrupRequest.getMatterID() == null || execInterrupRequest.getMatterID().isEmpty())) {
            execInterrupResponse = handleCustomerExecutionInterruption(execInterrupRequest);
        }
        if (!execInterrupRequest.getCustomerNr().toString().isEmpty() && execInterrupRequest.getMatterID() != null && !execInterrupRequest.getMatterID().isEmpty()) {
            execInterrupResponse = handleMatterExecutionInterruption(execInterrupRequest);
        }
        return execInterrupResponse;
    }

    private ExecInterrupResponse handleCustomerExecutionInterruption(ExecInterrupRequest execInterrupRequest) {
        log.info("Handling of customer Execution Interruption for customer nr: {} started.", execInterrupRequest.getCustomerNr());

        switch (execInterrupRequest.getExecInterrupType()) {
            case CUSTOMER_DECEASED -> handleCustomerDeceased(execInterrupRequest);
            default ->
                    throw new InternalException("handleCustomerExecutionInterruption: Default ExecInterrup enums not matched ");
        }

        return new ExecInterrupResponse();
    }

    private ExecInterrupResponse handleMatterExecutionInterruption(ExecInterrupRequest execInterrupRequest) {
        return new ExecInterrupResponse();
    }

    private void handleCustomerDeceased(ExecInterrupRequest execInterrupRequest) {
        log.info("Started handleCustomerDeceased for customer nr: {} ", execInterrupRequest.getCustomerNr());
        Optional<List<Charge>> oCharge = chargeRepository.findByCustomerNr(execInterrupRequest.getCustomerNr());
        oCharge.orElseThrow(() -> new ChargeMissingForCustomerNrException(execInterrupRequest.getCustomerNr()));
        oCharge.get().forEach(c -> {
            c.setChargeStatus(ChargeStatus.CUSTOMER_DECEASED);
            chargeRepository.save(c);
        });

    }
}
