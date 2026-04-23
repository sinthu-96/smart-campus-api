package com.smartcampus.exceptions;

/**
 * Thrown when a reading is posted to a sensor that is in MAINTENANCE or OFFLINE state.
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
