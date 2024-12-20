package dev.manyroads.matterreception.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class MatterCallbackUrlIsMissingException extends DCMException {
    public MatterCallbackUrlIsMissingException() {
        super("DCM-005: MatterRequest callback URL is missing");
    }
}
