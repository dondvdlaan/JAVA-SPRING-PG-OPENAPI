package dev.manyroads.casereception;

import dev.manyroads.model.CaseRequest;
import dev.manyroads.model.CaseResponse;
import dev.manyroads.verification.Verification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@Slf4j
public class CaseReceptionController {

    Verification verification;
    CaseReceptionService caseReceptionService;

    @RequestMapping(value = "/v1/cases", method = RequestMethod.POST)
    public ResponseEntity<CaseResponse> receiveCase(@RequestBody CaseRequest caseRequest){

        verification.verifyCaseRequest(caseRequest);
        CaseResponse caseResponse = caseReceptionService.processIncomingCaseRequest(caseRequest);

        System.out.println("caseRequest: " + caseRequest);
        return ResponseEntity.ok(new CaseResponse());
    }



}
