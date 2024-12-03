package dev.manyroads.intermediatereport.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class IntermediateReportStatusMissingMattersException extends DCMException {
    public IntermediateReportStatusMissingMattersException() {
        super("DCM-304: IntermediateReportStatus missing matters");
    }
}
