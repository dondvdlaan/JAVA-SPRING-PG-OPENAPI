package dev.manyroads.execinterrup.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public final class MatterMissingForCustomerNrException extends DCMException {
    public MatterMissingForCustomerNrException(String matterID, Long customerNr) {
        super(String.format("DCM-206: ExecInterrup No Matter with id %s found for CustomerNr: %d", matterID, customerNr));
    }
}
