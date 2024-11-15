package dev.manyroads.verification;

import dev.manyroads.exception.CaseIDIsMissingException;
import dev.manyroads.exception.CaseRequestEmptyOrNullException;
import dev.manyroads.exception.PersonIDIsMissingException;
import dev.manyroads.model.CaseRequest;
import org.springframework.stereotype.Service;

@Service
public class Verification {

    public void verifyCaseRequest(CaseRequest caseRequest) {
        if (caseRequest == null)
            throw new CaseRequestEmptyOrNullException();
        if (caseRequest.getPersonID() == null || caseRequest.getPersonID().toString().isEmpty())
            throw new PersonIDIsMissingException();
        if (caseRequest.getCaseID() == null || caseRequest.getCaseID().isEmpty())
            throw new CaseIDIsMissingException();
    }


}
