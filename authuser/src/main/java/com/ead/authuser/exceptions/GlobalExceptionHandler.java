package com.ead.authuser.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorRecordResponse> handleNotFoundException(
            NotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorRecordResponse> handleConflictException(
            ConflictException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }


    private ResponseEntity<ErrorRecordResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ErrorRecordResponse error = ErrorRecordResponse.builder()
                .status(status.value())
                .message(message)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(error);
    }
}
