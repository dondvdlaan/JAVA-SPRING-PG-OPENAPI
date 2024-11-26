package dev.manyroads.execinterrup.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class ExecInterrupEmptyOrNullException extends DCMException {
    public ExecInterrupEmptyOrNullException() {
        super("DCM-201: ExecInterrupRequest empty or Null");
    }
}
