package dev.manyroads.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ErrorData(
        Instant timeNow,
        Integer statusError,
        String errorType,
        String errorMessage
) {
    public ErrorData(HttpStatus statusError, String errorMessage) {
        this(
                Instant.now(),
                statusError.value(),
                statusError.getReasonPhrase(),
                errorMessage
        );
    }
}
