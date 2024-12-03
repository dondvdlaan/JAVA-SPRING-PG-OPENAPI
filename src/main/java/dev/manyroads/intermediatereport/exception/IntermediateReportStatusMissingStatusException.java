package dev.manyroads.intermediatereport.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class IntermediateReportStatusMissingStatusException extends DCMException {
    public IntermediateReportStatusMissingStatusException() {
        super("DCM-302: IntermediateReportStatus missing status");
    }
}