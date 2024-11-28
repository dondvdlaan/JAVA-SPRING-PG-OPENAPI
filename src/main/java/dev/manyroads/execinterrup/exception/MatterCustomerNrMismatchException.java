package dev.manyroads.execinterrup.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public final class MatterCustomerNrMismatchException extends DCMException {
    public MatterCustomerNrMismatchException(String matterID, Long customerNr) {
        super(String.format("DCM-208: ExecInterrup Matter with id %s not found for CustomerNr: %d", matterID, customerNr));
    }
}
