package dev.manyroads.verification;

import dev.manyroads.matterreception.exception.CaseIDIsMissingException;
import dev.manyroads.matterreception.exception.CaseRequestEmptyOrNullException;
import dev.manyroads.matterreception.exception.PersonIDIsMissingException;
import dev.manyroads.model.MatterRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Verification {

    /**
     * Verify incoming request case on null or empty object/fields, if erroneous send back a 400 BAD REQUEST to
     * the requesting microservice
     * @param caseRequest
     */
    public void verifyCaseRequest(MatterRequest caseRequest) {
        Optional.ofNullable(caseRequest)
                .orElseThrow(CaseRequestEmptyOrNullException::new);
        Optional.ofNullable(caseRequest.getCustomerNr())
                .orElseThrow(PersonIDIsMissingException::new);
        Optional.ofNullable(caseRequest.getMatterID())
                .orElseThrow(CaseIDIsMissingException::new);
    }


}
