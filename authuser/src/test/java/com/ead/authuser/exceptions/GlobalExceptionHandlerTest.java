package com.ead.authuser.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Mock private HttpServletRequest request;
    @Mock private MethodArgumentNotValidException validationException;
    @InjectMocks private GlobalExceptionHandler handler;

    @Test
    @DisplayName("handleNotFoundException should return 404 status")
    void handleNotFoundException_Returns404() {
        var ex = new NotFoundException("User not found");
        when(request.getRequestURI()).thenReturn("/users/123");

        var response = handler.handleNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("handleConflictException should return 409 status")
    void handleConflictException_Returns409() {
        var ex = new ConflictException("Username already taken");
        when(request.getRequestURI()).thenReturn("/auth/signup");

        var response = handler.handleConflictException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("handleUnauthorizedException should return 401 status")
    void handleUnauthorizedException_Returns401() {
        var ex = new UnauthorizedException("Invalid password");
        when(request.getRequestURI()).thenReturn("/users/123/password");

        var response = handler.handleUnauthorizedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("handleBusinessException should return 400 status")
    void handleBusinessException_Returns400() {
        var ex = new BusinessException("Invalid data");
        when(request.getRequestURI()).thenReturn("/auth/signup");

        var response = handler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("handleValidationException should return 400 status")
    void handleValidationException_Returns400() {
        when(validationException.getMessage()).thenReturn("Validation failed on field email");
        when(request.getRequestURI()).thenReturn("/auth/signup");

        var response = handler.handleValidationException(validationException, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("handleGenericException should return 500 status")
    void handleGenericException_Returns500() {
        var ex = new RuntimeException("Unexpected error");
        when(request.getRequestURI()).thenReturn("/users");

        var response = handler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("All handlers should return ResponseEntity with non-null body")
    void allHandlers_ReturnValidResponses() {
        when(request.getRequestURI()).thenReturn("/api/test");

        var notFound = handler.handleNotFoundException(new NotFoundException("Not found"), request);
        var conflict = handler.handleConflictException(new ConflictException("Conflict"), request);
        var unauthorized = handler.handleUnauthorizedException(new UnauthorizedException("Unauthorized"), request);
        var business = handler.handleBusinessException(new BusinessException("Error"), request);
        var generic = handler.handleGenericException(new RuntimeException("Error"), request);

        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(unauthorized.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(business.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(generic.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

