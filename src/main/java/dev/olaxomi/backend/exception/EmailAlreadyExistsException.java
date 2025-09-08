package dev.olaxomi.backend.exception;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException(String email) {
        super("Email " + email + " is already registered.");
    }
}
