package dev.manyroads.decomreception.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)
public final class InternalTechnicalException extends DCMException {
    public InternalTechnicalException(String message) {
        super(message);
    }
}
