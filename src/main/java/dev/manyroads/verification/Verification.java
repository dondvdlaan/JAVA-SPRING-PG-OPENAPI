package dev.manyroads.verification;

import dev.manyroads.execinterrup.exception.ExecInterrupTypeMissingException;
import dev.manyroads.execinterrup.exception.ExecInterrupTypeNotPartDomainException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusEmptyOrNullException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMissingChargeNrException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMissingMattersException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMissingStatusException;
import dev.manyroads.matterreception.exception.MatterIDIsMissingException;
import dev.manyroads.matterreception.exception.MatterRequestEmptyOrNullException;
import dev.manyroads.matterreception.exception.CustomerNrIsMissingException;
import dev.manyroads.execinterrup.exception.ExecInterrupEmptyOrNullException;
import dev.manyroads.model.ExecInterrupEnum;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.IntermediateReportStatusRequest;
import dev.manyroads.model.MatterRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class Verification {

    public void verifyMatterRequest(MatterRequest matterRequest) {
        Optional.ofNullable(matterRequest)
                .orElseThrow(MatterRequestEmptyOrNullException::new);
        Optional.ofNullable(matterRequest.getCustomerNr())
                .orElseThrow(CustomerNrIsMissingException::new);
        Optional.ofNullable(matterRequest.getMatterNr())
                .orElseThrow(MatterIDIsMissingException::new);
    }

    public void verifyExecInterrupRequest(ExecInterrupRequest execInterrupRequest) {
        Optional.ofNullable(execInterrupRequest)
                .orElseThrow(ExecInterrupEmptyOrNullException::new);
        Optional.ofNullable(execInterrupRequest.getCustomerNr())
                .orElseThrow(dev.manyroads.execinterrup.exception.CustomerNrIsMissingException::new);
        Optional.ofNullable(execInterrupRequest.getExecInterrupType())
                .orElseThrow(ExecInterrupTypeMissingException::new);
        if (Arrays.stream(ExecInterrupEnum.values()).noneMatch(v -> v.equals(execInterrupRequest.getExecInterrupType()))) {
            throw new ExecInterrupTypeNotPartDomainException();
        }
    }

    public void verifyIntermediateReportStatus(IntermediateReportStatusRequest intermediateReportStatusRequest) {
        if (Optional.ofNullable(intermediateReportStatusRequest).isEmpty())
            throw new IntermediateReportStatusEmptyOrNullException();
        if (Optional.ofNullable(intermediateReportStatusRequest.getChargeNr()).isEmpty())
            throw new IntermediateReportStatusMissingChargeNrException();
        if (Optional.ofNullable(intermediateReportStatusRequest.getStatusIntermediateReport()).isEmpty())
            throw new IntermediateReportStatusMissingStatusException();
        if (intermediateReportStatusRequest.getMattersIntermediateReport()== null || intermediateReportStatusRequest.getMattersIntermediateReport().isEmpty() )
            throw new IntermediateReportStatusMissingMattersException();
    }
}
