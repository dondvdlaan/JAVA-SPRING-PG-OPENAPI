package dev.manyroads.verification;

import dev.manyroads.execinterrup.exception.ExecInterrupTypeMissingException;
import dev.manyroads.execinterrup.exception.ExecInterrupTypeNotPartDomainException;
import dev.manyroads.matterreception.exception.MatterIDIsMissingException;
import dev.manyroads.matterreception.exception.MatterRequestEmptyOrNullException;
import dev.manyroads.matterreception.exception.CustomerNrIsMissingException;
import dev.manyroads.execinterrup.exception.ExecInterrupEmptyOrNullException;
import dev.manyroads.model.ExecInterrupEnum;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.MatterRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class Verification {

    /**
     * Verify incoming request matter on null or empty object/fields, if erroneous send back a 400 BAD REQUEST to
     * the requesting microservice
     *
     * @param matterRequest
     */
    public void verifyMatterRequest(MatterRequest matterRequest) {
        Optional.ofNullable(matterRequest)
                .orElseThrow(MatterRequestEmptyOrNullException::new);
        Optional.ofNullable(matterRequest.getCustomerNr())
                .orElseThrow(CustomerNrIsMissingException::new);
        Optional.ofNullable(matterRequest.getMatterID())
                .orElseThrow(MatterIDIsMissingException::new);
    }

    /**
     * Verify incoming Execution Interruptions on null or empty object/fields, if erroneous send back a 400 BAD REQUEST to
     * the requesting microservice
     *
     * @param execInterrupRequest
     */
    public void verifyExecInterrupRequest(ExecInterrupRequest execInterrupRequest) {
        Optional.ofNullable(execInterrupRequest)
                .orElseThrow(ExecInterrupEmptyOrNullException::new);
        Optional.ofNullable(execInterrupRequest.getCustomerNr())
                .orElseThrow(dev.manyroads.execinterrup.exception.CustomerNrIsMissingException::new);
        Optional.ofNullable(execInterrupRequest.getExecInterrupType())
                .orElseThrow(ExecInterrupTypeMissingException::new);
        if(Arrays.stream(ExecInterrupEnum.values()).noneMatch(v->v.equals(execInterrupRequest.getExecInterrupType()))){
            throw new ExecInterrupTypeNotPartDomainException();
        }
    }
}
