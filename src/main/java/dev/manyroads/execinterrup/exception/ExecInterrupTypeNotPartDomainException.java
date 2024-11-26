package dev.manyroads.execinterrup.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class ExecInterrupTypeNotPartDomainException extends DCMException {
    public ExecInterrupTypeNotPartDomainException() {
        super("DCM-204: ExecInterrupRequest Type not part of domain is missing");
    }
}
