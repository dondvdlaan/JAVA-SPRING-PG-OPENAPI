package dev.manyroads.matterreception.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class NoChargesFoundForCustomerException extends DCMException {
    public NoChargesFoundForCustomerException(Long customerNr) {
        super(String.format("DCM-006: No charges found for customer: %d", customerNr));
    }
}
