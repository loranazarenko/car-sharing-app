package carsharingapp.exception;

public class TelegramNotificationException extends RuntimeException {
    public TelegramNotificationException(String message) {
        super(message);
    }
}
