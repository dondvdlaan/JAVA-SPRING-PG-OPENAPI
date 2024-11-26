package dev.manyroads.matterreception.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class MatterIDIsMissingException extends DCMException {
    public MatterIDIsMissingException() {
        super("DCM-003: CaseRequest CaseID is missing");
    }
}
