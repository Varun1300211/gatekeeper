package com.gatekeeper.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLock(OptimisticLockException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", exception.getMessage()));
    }
}
