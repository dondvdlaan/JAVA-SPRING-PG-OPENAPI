package dev.manyroads.decomreception.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class AdminClientException extends DCMException {
    public AdminClientException() {
        super("DCM-004: No vehice type received");
    }
}
