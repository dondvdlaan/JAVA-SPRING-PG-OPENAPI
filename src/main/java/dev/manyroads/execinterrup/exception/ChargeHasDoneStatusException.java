package dev.manyroads.execinterrup.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public final class ChargeHasDoneStatusException extends DCMException {
    public ChargeHasDoneStatusException(Long customerNr) {
        super(String.format("DCM-207: ExecInterrup Charge had done status for CustomerNr: %d", customerNr));
    }
}
