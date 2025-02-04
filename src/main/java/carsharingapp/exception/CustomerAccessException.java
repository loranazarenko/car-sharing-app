package carsharingapp.exception;

public class CustomerAccessException extends RuntimeException {
    public CustomerAccessException(String message) {
        super(message);
    }
}
