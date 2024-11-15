package dev.manyroads.casereception;

import dev.manyroads.model.CaseRequest;
import dev.manyroads.model.CaseResponse;
import dev.manyroads.verification.Verification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@AllArgsConstructor
public class CaseController {

    Verification verification;

    @RequestMapping(value = "/v1/cases", method = RequestMethod.POST)
    public ResponseEntity<CaseResponse> receiveCase(@RequestBody CaseRequest caseRequest){

        verification.verifyCaseRequest(caseRequest);

        System.out.println("caseRequest: " + caseRequest);
        return ResponseEntity.ok(new CaseResponse());
    }



}
