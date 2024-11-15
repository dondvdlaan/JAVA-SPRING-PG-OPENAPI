package dev.manyroads.exception;


public abstract class DCMException extends RuntimeException {
    protected final String message;

    public DCMException(final String message) {
        this.message = message;
    }

    public final String getMessage()
    {return this.message;}
}
