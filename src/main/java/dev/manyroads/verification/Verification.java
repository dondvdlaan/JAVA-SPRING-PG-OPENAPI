package dev.manyroads.verification;

import dev.manyroads.casereception.exception.CaseIDIsMissingException;
import dev.manyroads.casereception.exception.CaseRequestEmptyOrNullException;
import dev.manyroads.casereception.exception.PersonIDIsMissingException;
import dev.manyroads.model.CaseRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Verification {

    /**
     * Verify incoming request case on null or empty object/fields, if erroneous send back a 400 BAD REQUEST to
     * the requesting microservice
     * @param caseRequest
     */
    public void verifyCaseRequest(CaseRequest caseRequest) {
        Optional.ofNullable(caseRequest)
                .orElseThrow(CaseRequestEmptyOrNullException::new);
        Optional.ofNullable(caseRequest.getCustomerID())
                .orElseThrow(PersonIDIsMissingException::new);
        Optional.ofNullable(caseRequest.getCaseID())
                .orElseThrow(CaseIDIsMissingException::new);
    }


}
