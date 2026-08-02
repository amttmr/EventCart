package com.eventcart.notification.exception;

/**
 * Exception thrown when a notification cannot be found.
 */
public class NotificationNotFoundException extends RuntimeException {
    /**
     * Creates a not-found exception.
     *
     * @param message error message
     */
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
