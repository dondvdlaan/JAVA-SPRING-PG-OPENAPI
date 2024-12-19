package dev.manyroads.matterreception.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class MatterRequestCustomerNrIsMissingException extends DCMException {
    public MatterRequestCustomerNrIsMissingException() {
        super("DCM-002: MatterRequest CustomerNr is missing");
    }
}
