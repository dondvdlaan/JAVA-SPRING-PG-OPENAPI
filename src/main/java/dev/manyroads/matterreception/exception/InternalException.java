package dev.manyroads.matterreception.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)
public final class InternalException extends DCMException {
    public InternalException(String message) {
        super(message);
    }
}
