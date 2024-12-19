package dev.manyroads.decomreception;

import dev.manyroads.execinterrup.ExecutionInterruptionService;
import dev.manyroads.intermediatereport.IntermediateReportStatusService;
import dev.manyroads.matterreception.MatterReceptionService;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.ExecInterrupResponse;
import dev.manyroads.model.IntermediateReportStatusRequest;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.verification.Verification;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Slf4j
public class DecomReceptionController {

    Verification verification;
    MatterReceptionService matterReceptionService;
    ExecutionInterruptionService executionInterruptionService;
    IntermediateReportStatusService intermediateReportStatusService;

    @RequestMapping(value = "/v1/matters", method = RequestMethod.POST)
    public ResponseEntity<MatterResponse> receiveMatter(@RequestBody MatterRequest matterRequest, HttpServletRequest httpServletRequest) {

        verification.verifyMatterRequest(matterRequest,httpServletRequest);
        var terminationCallBackUrl = httpServletRequest.getHeader("Termination-Call-Back-Url");
        System.out.println("terminationCallBackUrl: " + terminationCallBackUrl);
        MatterResponse matterResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        log.info("Response returned: {}", matterResponse);
        return ResponseEntity.ok(matterResponse);
    }

    @RequestMapping(value = "/v1/execinterrup", method = RequestMethod.POST)
    public ResponseEntity<ExecInterrupResponse> receiveExecutionInterrupts(@RequestBody ExecInterrupRequest execInterrupRequest) {

        verification.verifyExecInterrupRequest(execInterrupRequest);
        ExecInterrupResponse execInterrupResponse = executionInterruptionService.processIncomingExecutionInterruptions(execInterrupRequest);

        log.info("execInterrupResponse returned: {}", execInterrupResponse);
        return ResponseEntity.ok(execInterrupResponse);
    }

    @RequestMapping(value = "/v1/charges/intermediatereportstatus", method = RequestMethod.POST)
    public ResponseEntity<?> receiveIntermediateReportStatus(@RequestBody IntermediateReportStatusRequest intermediateReportStatusRequest) {

        verification.verifyIntermediateReportStatus(intermediateReportStatusRequest);
        intermediateReportStatusService.processIntermediateReportStatusRequests(intermediateReportStatusRequest);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public String testing(){
        log.info("testing: GET test started");
        return "Holita";
    }

}
