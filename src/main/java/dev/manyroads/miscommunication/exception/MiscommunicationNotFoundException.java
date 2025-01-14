package dev.manyroads.miscommunication.exception;

import dev.manyroads.decomreception.exception.DCMException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public final class MiscommunicationNotFoundException extends DCMException {
    public MiscommunicationNotFoundException(UUID misCommID) {
        super(String.format("DCM-401: Miscommunication: %s not found", misCommID));
    }
}
