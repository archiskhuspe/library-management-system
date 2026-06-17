package com.example.lms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super(String.format("A member with email '%s' already exists.", email));
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
} 