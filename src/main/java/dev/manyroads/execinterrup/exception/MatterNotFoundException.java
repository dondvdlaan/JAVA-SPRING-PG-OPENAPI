package dev.manyroads.execinterrup.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public final class MatterNotFoundException extends DCMException {
    public MatterNotFoundException(String matterNr, Long customerNr) {
        super(String.format("DCM-209: ExecInterrup Matter with nr %s not found for CustomerNr: %d", matterNr, customerNr));
    }
}
