package dev.manyroads.decomreception.exception;

import dev.manyroads.execinterrup.exception.ChargeHasDoneStatusException;
import dev.manyroads.execinterrup.exception.ChargeMissingForCustomerNrException;
import dev.manyroads.execinterrup.exception.MatterCustomerNrMismatchException;
import dev.manyroads.execinterrup.exception.MatterMissingForCustomerNrException;
import dev.manyroads.matterreception.exception.CustomerNrIsMissingException;
import dev.manyroads.matterreception.exception.MatterIDIsMissingException;
import dev.manyroads.matterreception.exception.MatterRequestEmptyOrNullException;
import dev.manyroads.matterreception.exception.VehicleTypeNotCoincideWithDomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class DCMExceptionHandler {

    @ExceptionHandler({
            MatterRequestEmptyOrNullException.class,
            CustomerNrIsMissingException.class,
            MatterIDIsMissingException.class,
            AdminClientException.class,
            VehicleTypeNotCoincideWithDomainException.class,
            VehicleTypeNotFoundException.class,
            InternalException.class,
            ChargeMissingForCustomerNrException.class,
            MatterMissingForCustomerNrException.class,
            ChargeHasDoneStatusException.class,
            MatterCustomerNrMismatchException.class
    })
    public ResponseEntity<ErrorData> handleException(final DCMException ex) {
        log.error(String.format("Error from handleException: %s %s", ex.getClass().getSimpleName(), ex.getMessage()));
        return ResponseEntity.badRequest().body(
                new ErrorData(HttpStatus.BAD_REQUEST, ex.getMessage())
        );
    }
}
