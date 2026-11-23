package com.reloop.exception;

/**
 * Base class for every RELOOP-specific problem.
 * It is a CHECKED exception (extends Exception), so the compiler forces
 * callers to either catch it or declare it with "throws".
 */
public class ReloopException extends Exception {

    public ReloopException(String message) {
        super(message);
    }

    // "cause" keeps the original error (e.g. a SQLException) for debugging
    public ReloopException(String message, Throwable cause) {
        super(message, cause);
    }
}
