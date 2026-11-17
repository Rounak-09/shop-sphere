package com.shopsphere.product.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {}

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ProductNotFoundException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ApiError> handleDuplicateSku(
            DuplicateSkuException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> errors =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                error -> error.getField(),
                                error -> error.getDefaultMessage() == null
                                        ? "Invalid value"
                                        : error.getDefaultMessage(),
                                (first, second) -> first
                        ));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                request.getRequestURI(),
                errors
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(
            OptimisticLockingFailureException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "Product was modified by another transaction. Please retry.",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "The request conflicts with existing product data.",
                request.getRequestURI(),
                null
        );
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> validationErrors) {

        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                validationErrors
        );

        return ResponseEntity.status(status).body(error);
    }
}