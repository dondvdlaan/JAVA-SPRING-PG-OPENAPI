package dev.manyroads.matterreception;

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
public class MatterReceptionController {

    Verification verification;
    MatterReceptionService matterReceptionService;

    @RequestMapping(value = "/v1/matters", method = RequestMethod.POST)
    public ResponseEntity<MatterResponse> receiveCase(@RequestBody MatterRequest caseRequest){

        verification.verifyCaseRequest(caseRequest);
        MatterResponse caseResponse = matterReceptionService.processIncomingMatterRequest(caseRequest);

        System.out.println("caseRequest: " + caseRequest);
        return ResponseEntity.ok(new MatterResponse());
    }



}
