package dev.manyroads.execinterrup.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class ExecInterrupTypeMissingException extends DCMException {
    public ExecInterrupTypeMissingException() {
        super("DCM-203: ExecInterrupRequest Type is missing");
    }
}
