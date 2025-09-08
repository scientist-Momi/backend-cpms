package dev.olaxomi.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        // Log full details (for devs)
        ex.printStackTrace();

        // Hide sensitive info from users
        ApiError error = new ApiError("Invalid request data. Please check your input.", null);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Catch generic JPA/SQL errors
    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ApiError> handlePersistenceException(PersistenceException ex) {
        ex.printStackTrace();
        ApiError error = new ApiError("A database error occurred. Please try again later.", null);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Fallback for all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex) {
        ex.printStackTrace();
        ApiError error = new ApiError("An unexpected error occurred.", null);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailExists(EmailAlreadyExistsException ex) {
        ApiError error = new ApiError(ex.getMessage(), null);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }


    static class ApiError {
        private String message;
        private Object data;

        public ApiError(String message, Object data) {
            this.message = message;
            this.data = data;
        }

        // getters & setters
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }
}
