package dev.manyroads.verification;

import dev.manyroads.exception.CaseIDIsMissingException;
import dev.manyroads.exception.CaseRequestEmptyOrNullException;
import dev.manyroads.exception.PersonIDIsMissingException;
import dev.manyroads.model.CaseRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Verification {

    public void verifyCaseRequest(CaseRequest caseRequest) {
        Optional.ofNullable(caseRequest)
                .orElseThrow(CaseRequestEmptyOrNullException::new);
        Optional.ofNullable(caseRequest.getPersonID())
                .orElseThrow(PersonIDIsMissingException::new);
        Optional.ofNullable(caseRequest.getCaseID())
                .orElseThrow(CaseIDIsMissingException::new);
    }


}
