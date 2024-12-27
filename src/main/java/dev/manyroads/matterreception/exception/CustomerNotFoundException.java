package dev.manyroads.matterreception.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class CustomerNotFoundException extends DCMException {
    public CustomerNotFoundException(Long customerNr) {
        super(String.format("DCM-007: Customer: %d not found", customerNr));
    }
}
