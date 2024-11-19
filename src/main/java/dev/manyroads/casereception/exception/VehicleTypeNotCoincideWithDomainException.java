package dev.manyroads.casereception.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class VehicleTypeNotCoincideWithDomainException extends DCMException {
    public VehicleTypeNotCoincideWithDomainException() {
        super("DCM-006: Vehicle type does not coincide with domain");
    }
}
