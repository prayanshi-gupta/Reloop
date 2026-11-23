package com.reloop.exception;

/**
 * Thrown when user input fails validation rules
 * (e.g., negative weight, empty field, invalid email format).
 */
public class ValidationException extends ReloopException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
