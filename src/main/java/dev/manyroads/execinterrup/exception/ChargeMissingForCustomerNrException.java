package dev.manyroads.execinterrup.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public final class ChargeMissingForCustomerNrException extends DCMException {
    public ChargeMissingForCustomerNrException(Long customerNr) {
        super(String.format("DCM-205: ExecInterrup No Charge found for CustomerNr: %d", customerNr));
    }
}
