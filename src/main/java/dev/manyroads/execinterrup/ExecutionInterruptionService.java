package dev.manyroads.execinterrup;

import dev.manyroads.decomreception.exception.InternalException;
import dev.manyroads.execinterrup.exception.ChargeHasDoneStatusException;
import dev.manyroads.execinterrup.exception.ChargeMissingForCustomerNrException;
import dev.manyroads.execinterrup.exception.MatterCustomerNrMismatchException;
import dev.manyroads.execinterrup.exception.MatterMissingForCustomerNrException;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.ExecInterrupResponse;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.ExecInterrup;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.ChargeStatus;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.ExecInterrupRepository;
import dev.manyroads.model.repository.MatterRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


@Service
@AllArgsConstructor
@Slf4j
public class ExecutionInterruptionService {

    ChargeRepository chargeRepository;
    MatterRepository matterRepository;
    ExecInterrupRepository execInterrupRepository;

    public ExecInterrupResponse processIncomingExecutionInterruptions(ExecInterrupRequest execInterrupRequest) {
        log.info("Processing of Execution Interruption for customer nr: {} started.", execInterrupRequest.getCustomerNr());
        saveExecInterrupRequest(execInterrupRequest);
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
            // TODO: case REJECTED -> handleCustomerChargeRejected(execInterrupRequest);
            default ->
                    throw new InternalException("handleCustomerExecutionInterruption: Default ExecInterrup enums not matched ");
        }

        return new ExecInterrupResponse();
    }

    private ExecInterrupResponse handleMatterExecutionInterruption(ExecInterrupRequest execInterrupRequest) {
        log.info("Handling of matter Execution Interruption for customer nr: {} started.", execInterrupRequest.getCustomerNr());
        Optional<Matter> oMatter = matterRepository.findById(UUID.fromString(execInterrupRequest.getMatterID()));
        oMatter.orElseThrow(() -> new InternalException(String.format("Matter with id: %s not found", execInterrupRequest.getMatterID())));
        if (!Objects.equals(oMatter.get().getCustomerNr(), execInterrupRequest.getCustomerNr())) {
            throw new MatterCustomerNrMismatchException(oMatter.get().getMatterID().toString(), execInterrupRequest.getCustomerNr());
        }

        switch (execInterrupRequest.getExecInterrupType()) {
            case WITHDRAWN -> handleMatterWithdrawn(execInterrupRequest);
            default ->
                    throw new InternalException("handleMatterExecutionInterruption: Default ExecInterrup enums not matched ");
        }

        return new ExecInterrupResponse();
    }

    private void handleCustomerDeceased(ExecInterrupRequest execInterrupRequest) {
        log.info("Started handleCustomerDeceased for customer nr: {} ", execInterrupRequest.getCustomerNr());
        Optional<List<Charge>> oChargeList = chargeRepository.findByCustomerNr(execInterrupRequest.getCustomerNr());
        oChargeList.orElseThrow(() -> new ChargeMissingForCustomerNrException(execInterrupRequest.getCustomerNr()));
        // Filter charges for status booked
        oChargeList.get().forEach(c -> {
            c.setChargeStatus(ChargeStatus.CUSTOMER_DECEASED);
            chargeRepository.save(c);
        });
    }

    private void handleMatterWithdrawn(ExecInterrupRequest execInterrupRequest) {
        log.info("Started handleMatterWithdrawn for customer nr: {} ", execInterrupRequest.getCustomerNr());
        Optional<Matter> oMatter = matterRepository.findById(UUID.fromString(execInterrupRequest.getMatterID()));
        oMatter.orElseThrow(() -> new MatterMissingForCustomerNrException(execInterrupRequest.getMatterID(), execInterrupRequest.getCustomerNr()));
        if (oMatter.get().getCharge().getChargeStatus() == ChargeStatus.DONE) {
            throw new ChargeHasDoneStatusException(execInterrupRequest.getCustomerNr());
        }
        oMatter.get().setMatterStatus(MatterStatus.WITHDRAWN);
        matterRepository.save(oMatter.get());
    }

    private void saveExecInterrupRequest(ExecInterrupRequest execInterrupRequest) {
        ExecInterrup execInterrup = ExecInterrup
                .builder()
                .customerNr(execInterrupRequest.getCustomerNr())
                .matterID(execInterrupRequest.getMatterID())
                .execInterrupStatus(execInterrupRequest.getExecInterrupType())
                .build();
        execInterrupRepository.save(execInterrup);
    }
}
