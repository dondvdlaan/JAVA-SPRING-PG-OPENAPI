package dev.manyroads.matterreception.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class VehicleTypeNotFoundException extends DCMException {
    public VehicleTypeNotFoundException() {
        super("DCM-005: Vehicle type not found");
    }
}
