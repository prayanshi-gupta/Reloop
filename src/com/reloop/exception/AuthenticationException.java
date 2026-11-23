package com.reloop.exception;

/**
 * Thrown when user credentials fail or an unauthorized action is attempted.
 */
public class AuthenticationException extends ReloopException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
