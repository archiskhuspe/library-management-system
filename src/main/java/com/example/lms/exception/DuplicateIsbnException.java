package com.example.lms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateIsbnException extends RuntimeException {

    public DuplicateIsbnException(String isbn) {
        super(String.format("A book with ISBN '%s' already exists.", isbn));
    }

    public DuplicateIsbnException(String message, Throwable cause) {
        super(message, cause);
    }
} 