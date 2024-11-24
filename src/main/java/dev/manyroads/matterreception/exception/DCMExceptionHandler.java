package dev.manyroads.matterreception.exception;

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
            PersonIDIsMissingException.class,
            MatterIDIsMissingException.class,
            AdminClientException.class,
            VehicleTypeNotCoincideWithDomainException.class,
            VehicleTypeNotFoundException.class
    })
    public ResponseEntity<ErrorData> handleException(final DCMException ex) {
        log.error(String.format("Error from handleException: %s %s", ex.getClass().getSimpleName(), ex.getMessage()));
        return ResponseEntity.badRequest().body(
                new ErrorData(HttpStatus.BAD_REQUEST, ex.getMessage())
        );
    }
}
