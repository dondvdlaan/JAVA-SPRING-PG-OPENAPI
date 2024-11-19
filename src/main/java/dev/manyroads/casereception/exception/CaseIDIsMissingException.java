package dev.manyroads.casereception.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class CaseIDIsMissingException extends DCMException {
    public CaseIDIsMissingException() {
        super("DCM-003: CaseRequest CaseID is missing");
    }
}
