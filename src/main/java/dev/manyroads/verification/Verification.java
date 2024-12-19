package dev.manyroads.verification;

import dev.manyroads.execinterrup.exception.ExecInterrupTypeMissingException;
import dev.manyroads.execinterrup.exception.ExecInterrupTypeNotPartDomainException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusEmptyOrNullException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMissingChargeNrException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMissingMattersException;
import dev.manyroads.intermediatereport.exception.IntermediateReportStatusMissingStatusException;
import dev.manyroads.matterreception.exception.MatterIDIsMissingException;
import dev.manyroads.matterreception.exception.MatterRequestEmptyOrNullException;
import dev.manyroads.matterreception.exception.MatterRequestCustomerNrIsMissingException;
import dev.manyroads.execinterrup.exception.ExecInterrupEmptyOrNullException;
import dev.manyroads.matterreception.exception.MatterRequestHeaderEmptyOrNullException;
import dev.manyroads.model.ExecInterrupEnum;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.IntermediateReportStatusRequest;
import dev.manyroads.model.MatterRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class Verification {

    public void verifyMatterRequest(MatterRequest matterRequest, HttpServletRequest httpServletRequest) {
        Optional.ofNullable(matterRequest)
                .orElseThrow(MatterRequestEmptyOrNullException::new);
        Optional.ofNullable(httpServletRequest.getHeader("Termination-Call-Back-Url"))
                .orElseThrow(MatterRequestHeaderEmptyOrNullException::new);
        Optional.ofNullable(matterRequest.getCustomerNr())
                .orElseThrow(MatterRequestCustomerNrIsMissingException::new);
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
        if (Optional.ofNullable(intermediateReportStatusRequest.getChargeID()).isEmpty())
            throw new IntermediateReportStatusMissingChargeNrException();
        if (Optional.ofNullable(intermediateReportStatusRequest.getStatusIntermediateReport()).isEmpty())
            throw new IntermediateReportStatusMissingStatusException();
        if (intermediateReportStatusRequest.getMattersIntermediateReport()== null || intermediateReportStatusRequest.getMattersIntermediateReport().isEmpty() )
            throw new IntermediateReportStatusMissingMattersException();
    }
}
