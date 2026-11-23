package com.reloop.exception;

/**
 * Thrown when an entity (User, Station, Material, Submission) is not found in the database.
 */
public class ResourceNotFoundException extends ReloopException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
