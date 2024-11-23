package dev.manyroads.matterreception.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class PersonIDIsMissingException extends DCMException {
    public PersonIDIsMissingException() {
        super("DCM-002: CaseRequest PersonID is missing");
    }
}
