package dev.manyroads.casereception.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class CaseRequestEmptyOrNullException extends DCMException {
    public CaseRequestEmptyOrNullException() {
        super("DCM-001: CaseRequest empty or Null");
    }
}
