package com.ead.authuser.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Mock private HttpServletRequest request;
    @InjectMocks private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    // ============ TESTS FOR ALL 7 EXCEPTION HANDLERS ============

    @Test
    @DisplayName("handleMethodArgumentTypeMismatch should return 400 with invalid UUID message")
    void handleMethodArgumentTypeMismatch_Returns400() {
        var exception = mock(MethodArgumentTypeMismatchException.class);

        var response = handler.handleMethodArgumentTypeMismatch(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Invalid UUID format");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleNotFoundException should return 404 with custom message")
    void handleNotFoundException_Returns404() {
        var ex = new NotFoundException("User not found");

        var response = handler.handleNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("User not found");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("handleValidationException with single field error should return 400 with field error")
    void handleValidationException_WithSingleError_Returns400() {
        var bindingResult = mock(BindingResult.class);
        var fieldError = mock(FieldError.class);
        var exception = mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        when(fieldError.getField()).thenReturn("fullName");
        when(fieldError.getDefaultMessage()).thenReturn("Full Name is mandatory");

        var response = handler.handleValidationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("fullName: Full Name is mandatory");
    }

    @Test
    @DisplayName("handleValidationException with multiple field errors should return first error")
    void handleValidationException_WithMultipleErrors_ReturnsFirstError() {
        var bindingResult = mock(BindingResult.class);
        var fieldError1 = mock(FieldError.class);
        var fieldError2 = mock(FieldError.class);
        var exception = mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError1, fieldError2));
        when(fieldError1.getField()).thenReturn("email");
        when(fieldError1.getDefaultMessage()).thenReturn("Email must be valid");

        var response = handler.handleValidationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("email: Email must be valid");
    }

    @Test
    @DisplayName("handleValidationException with no field errors should return default message")
    void handleValidationException_WithNoErrors_ReturnsDefaultMessage() {
        var bindingResult = mock(BindingResult.class);
        var exception = mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of());

        var response = handler.handleValidationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
    }

    @Test
    @DisplayName("handleConflictException should return 409 with custom message")
    void handleConflictException_Returns409() {
        var ex = new ConflictException("Username already taken");

        var response = handler.handleConflictException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).isEqualTo("Username already taken");
    }

    @Test
    @DisplayName("handleUnauthorizedException should return 401 with custom message")
    void handleUnauthorizedException_Returns401() {
        var ex = new UnauthorizedException("Invalid password");

        var response = handler.handleUnauthorizedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Invalid password");
    }

    @Test
    @DisplayName("handleBusinessException should return 400 with custom message")
    void handleBusinessException_Returns400() {
        var ex = new BusinessException("Invalid data");

        var response = handler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Invalid data");
    }

    @Test
    @DisplayName("handleGenericException should return 500 with generic message")
    void handleGenericException_Returns500() {
        var ex = new RuntimeException("Unexpected error");

        var response = handler.handleGenericException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).isEqualTo("Internal server error");
    }

    // ============ INTEGRATION TESTS ============

    @Test
    @DisplayName("All handlers should populate ErrorRecordResponse correctly")
    void allHandlers_PopulateResponseCorrectly() {
        var notFoundResponse = handler.handleNotFoundException(
                new NotFoundException("Not found"), request
        );
        var conflictResponse = handler.handleConflictException(
                new ConflictException("Conflict"), request
        );
        var unauthorizedResponse = handler.handleUnauthorizedException(
                new UnauthorizedException("Unauthorized"), request
        );
        var businessResponse = handler.handleBusinessException(
                new BusinessException("Error"), request
        );
        var genericResponse = handler.handleGenericException(
                new RuntimeException("Error"), request
        );

        assertThat(notFoundResponse.getBody()).isNotNull()
                .hasFieldOrPropertyWithValue("status", 404)
                .hasFieldOrPropertyWithValue("path", "/api/test");
        assertThat(notFoundResponse.getBody().timestamp()).isNotNull();

        assertThat(conflictResponse.getBody()).isNotNull()
                .hasFieldOrPropertyWithValue("status", 409);

        assertThat(unauthorizedResponse.getBody()).isNotNull()
                .hasFieldOrPropertyWithValue("status", 401);

        assertThat(businessResponse.getBody()).isNotNull()
                .hasFieldOrPropertyWithValue("status", 400);

        assertThat(genericResponse.getBody()).isNotNull()
                .hasFieldOrPropertyWithValue("status", 500);
    }

    @Test
    @DisplayName("Response body should have consistent structure for all exceptions")
    void responseBody_ShouldHaveConsistentStructure() {
        var response = handler.handleNotFoundException(
                new NotFoundException("Test message"), request
        );

        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isGreaterThan(0);
        assertThat(body.message()).isNotBlank();
        assertThat(body.path()).isNotBlank();
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Validation exception should preserve first error field and message")
    void validationException_ShouldPreserveFieldAndMessage() {
        var bindingResult = mock(BindingResult.class);
        var fieldError = mock(FieldError.class);
        var exception = mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        when(fieldError.getField()).thenReturn("email");
        when(fieldError.getDefaultMessage()).thenReturn("must be a valid email");

        var response = handler.handleValidationException(exception, request);

        assertThat(response.getBody()).isNotNull();
        var message = response.getBody().message();
        assertThat(message).contains("email")
                .contains("must be a valid email")
                .contains(":");
    }
}

