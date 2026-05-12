package com.ead.course.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // VALIDATION ERROR (DTO)
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRecordResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value"),
                        (existing, replacement) -> existing
                ));

        log.warn("Validation failed for request: {} - Errors: {}", request.getRequestURI(), errors);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request,
                errors
        );
    }

    // =========================
    // INVALID PARAM (UUID, etc)
    // =========================
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorRecordResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = "Invalid parameter: " + ex.getName();
        log.warn("Parameter type mismatch for request: {} - Parameter: {} - Required type: {}",
                request.getRequestURI(), ex.getName(), ex.getRequiredType());

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    // =========================
    // BUSINESS EXCEPTIONS
    // =========================
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorRecordResponse> handleNotFound(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Resource not found for request: {} - Message: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorRecordResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        log.warn("Business conflict for request: {} - Message: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorRecordResponse> handleBusiness(
            BusinessException ex,
            HttpServletRequest request
    ) {
        log.warn("Business rule violation for request: {} - Message: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    //Futuramente para JWT e Auth
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorRecordResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        log.warn("Unauthorized access attempt for request: {} - Message: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    // =========================
    // DATABASE ERRORS
    // =========================
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRecordResponse> handleDataIntegrity(
            org.springframework.dao.DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        log.error("Data integrity violation for request: {} - Root cause: {}", request.getRequestURI(), ex.getRootCause() != null ? ex.getRootCause().getMessage() : "Unknown", ex);

        return buildResponse(
                HttpStatus.CONFLICT,
                "Data integrity violation",
                request
        );
    }

    @ExceptionHandler(org.hibernate.exception.ConstraintViolationException.class)
    public ResponseEntity<ErrorRecordResponse> handleConstraintViolation(
            org.hibernate.exception.ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        log.error("Database constraint violation for request: {} - Constraint: {} - Message: {}",
                request.getRequestURI(), ex.getConstraintName(), ex.getMessage(), ex);

        return buildResponse(
                HttpStatus.CONFLICT,
                "Database constraint violation",
                request
        );
    }

    // =========================
    // GENERIC ERROR (FALLBACK)
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRecordResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error for request: {} - Exception: {}", request.getRequestURI(), ex.getClass().getSimpleName(), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                request
        );
    }

    // =========================
    // BUILDER
    // =========================
    private ResponseEntity<ErrorRecordResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        return buildResponse(status, message, request, null);
    }

    private ResponseEntity<ErrorRecordResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> errorDetails
    ) {
        ErrorRecordResponse error = ErrorRecordResponse.builder()
                .status(status.value())
                .message(message)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .path(request.getRequestURI())
                .errorDetails(errorDetails)
                .build();

        return ResponseEntity.status(status).body(error);
    }
}