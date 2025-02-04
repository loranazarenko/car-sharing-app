package carsharingapp.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST);
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(this::getErrorMessage)
                .toList();
        body.put("errors", errors);
        return new ResponseEntity<>(body, headers, status);
    }

    private String getErrorMessage(ObjectError objectError) {
        String defaultMessage = objectError.getDefaultMessage();
        if (objectError instanceof FieldError) {
            String field = ((FieldError) objectError).getField();
            return field + " " + defaultMessage;
        }
        return defaultMessage;
    }

    @ExceptionHandler(RegistrationException.class)
    protected ResponseEntity<Object> handleRegistrationException(RegistrationException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", e.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", e.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(RentalException.class)
    public ResponseEntity<Object> handleClosedRentalException(RentalException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST);
        body.put("errors", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CarAvailableException.class)
    public ResponseEntity<Object> handleCarUnavailableException(CarAvailableException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST);
        body.put("errors", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomerAccessException.class)
    public ResponseEntity<Object> handleCustomerAccessError(CustomerAccessException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST);
        body.put("errors", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CreateSessionException.class)
    public ResponseEntity<Object> handleCreateSessionException(CreateSessionException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST);
        body.put("errors", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaidPaymentException.class)
    public ResponseEntity<Object> handlePaidPaymentException(PaidPaymentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST);
        body.put("errors", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST);
        body.put("errors", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TelegramNotificationException.class)
    public ResponseEntity<Object> handleTelegramNotificationException(
            TelegramNotificationException ex
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST);
        body.put("errors", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
