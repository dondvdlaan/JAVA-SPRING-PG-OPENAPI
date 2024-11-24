package dev.manyroads.matterreception.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class MatterRequestEmptyOrNullException extends DCMException {
    public MatterRequestEmptyOrNullException() {
        super("DCM-001: CaseRequest empty or Null");
    }
}
