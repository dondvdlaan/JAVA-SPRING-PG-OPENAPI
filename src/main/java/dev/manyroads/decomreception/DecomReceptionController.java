package dev.manyroads.decomreception;

import dev.manyroads.matterreception.MatterReceptionService;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.ExecInterrupResponse;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.verification.Verification;
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

    @RequestMapping(value = "/v1/matters", method = RequestMethod.POST)
    public ResponseEntity<MatterResponse> receiveMatter(@RequestBody MatterRequest matterRequest) {

        verification.verifyMatterRequest(matterRequest);
        MatterResponse caseResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        log.info("Response returned: {}", caseResponse);
        return ResponseEntity.ok(caseResponse);
    }

    @RequestMapping(value = "/v1/execinterrup", method = RequestMethod.POST)
    public ResponseEntity<ExecInterrupResponse> receiveExecutionInterrupts(@RequestBody ExecInterrupRequest execInterrupRequest) {

        verification.verifyExecInterrupRequest(execInterrupRequest);
        //MatterResponse caseResponse = matterReceptionService.processIncomingMatterRequest(matterRequest);

        //log.info("Response returned: {}", caseResponse);
        return ResponseEntity.ok(new ExecInterrupResponse());
    }


}
