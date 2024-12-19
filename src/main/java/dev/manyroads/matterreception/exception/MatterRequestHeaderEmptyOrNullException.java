package dev.manyroads.matterreception.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class MatterRequestHeaderEmptyOrNullException extends DCMException {
    public MatterRequestHeaderEmptyOrNullException() {
        super("DCM-005: matterRequest header empty or Null");
    }
}
