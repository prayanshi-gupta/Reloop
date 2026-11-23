package com.reloop.exception;

/**
 * Wraps low-level SQLException / IOException problems so the rest of the
 * application only deals with one clean, project-specific exception type.
 */
public class DatabaseException extends ReloopException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
