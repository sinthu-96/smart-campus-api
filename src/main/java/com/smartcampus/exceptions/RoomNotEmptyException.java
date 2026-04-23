package com.smartcampus.exceptions;

/**
 * Thrown when a room deletion is attempted while sensors are still assigned.
 * Mapped to HTTP 409 Conflict by RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
