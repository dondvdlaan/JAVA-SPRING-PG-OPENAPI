package dev.manyroads.execinterrup.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class CustomerNrIsMissingException extends DCMException {
    public CustomerNrIsMissingException() {
        super("DCM-202: ExecInterrupRequest CustomerNr is missing");
    }
}
