package com.booking.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.booking.dto.response.ErrorResponse;
import com.booking.dto.response.ValidationErrorResponse;
import com.booking.exceptionHandler.GlobalExceptionHandler;
import com.booking.exception.BadRequestException;
import com.booking.exception.DuplicateResourceException;
import com.booking.exception.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private ConstraintViolationException constraintViolationException;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();

        when(request.getRequestURI())
                .thenReturn("/api/test");
    }


    // =========================================================
    // RESOURCE NOT FOUND
    // =========================================================

    @Test
    void handleResourceNotFound_shouldReturn404() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException("Resource not found");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleResourceNotFound(
                        exception,
                        request
                );

        assertEquals(404, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                404,
                response.getBody().getStatus()
        );

        assertEquals(
                "Not Found",
                response.getBody().getError()
        );

        assertEquals(
                "Resource not found",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/test",
                response.getBody().getPath()
        );

        assertNotNull(response.getBody().getTimestamp());
    }


    // =========================================================
    // BAD REQUEST
    // =========================================================

    @Test
    void handleBadRequest_shouldReturn400() {

        BadRequestException exception =
                new BadRequestException("Invalid request");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleBadRequest(
                        exception,
                        request
                );

        assertEquals(400, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                400,
                response.getBody().getStatus()
        );

        assertEquals(
                "Bad Request",
                response.getBody().getError()
        );

        assertEquals(
                "Invalid request",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/test",
                response.getBody().getPath()
        );
    }


    // =========================================================
    // DUPLICATE RESOURCE
    // =========================================================

    @Test
    void handleDuplicateResource_shouldReturn409() {

        DuplicateResourceException exception =
                new DuplicateResourceException(
                        "Resource already exists"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleDuplicateResource(
                        exception,
                        request
                );

        assertEquals(409, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                409,
                response.getBody().getStatus()
        );

        assertEquals(
                "Conflict",
                response.getBody().getError()
        );

        assertEquals(
                "Resource already exists",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/test",
                response.getBody().getPath()
        );
    }


    // =========================================================
    // BAD CREDENTIALS
    // =========================================================

    @Test
    void handleBadCredentials_shouldReturn401() {

        BadCredentialsException exception =
                new BadCredentialsException(
                        "Authentication failed"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleBadCredentials(
                        exception,
                        request
                );

        assertEquals(401, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                401,
                response.getBody().getStatus()
        );

        assertEquals(
                "Unauthorized",
                response.getBody().getError()
        );

        /*
         * The handler deliberately does not expose
         * ex.getMessage().
         */
        assertEquals(
                "Invalid email or password",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/test",
                response.getBody().getPath()
        );
    }


    // =========================================================
    // ACCESS DENIED
    // =========================================================

    @Test
    void handleAccessDenied_shouldReturn403() {

        AccessDeniedException exception =
                new AccessDeniedException(
                        "Access is denied"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleAccessDenied(
                        exception,
                        request
                );

        assertEquals(403, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                403,
                response.getBody().getStatus()
        );

        assertEquals(
                "Forbidden",
                response.getBody().getError()
        );

        assertEquals(
                "Access is denied",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/test",
                response.getBody().getPath()
        );
    }


    // =========================================================
    // VALIDATION ERRORS
    // =========================================================

    @Test
    void handleValidationErrors_shouldReturn400WithFieldErrors()
            throws Exception {

        FieldError nameError =
                new FieldError(
                        "resourceRequest",
                        "name",
                        "Resource name is required"
                );

        FieldError priceError =
                new FieldError(
                        "resourceRequest",
                        "price",
                        "Price must be greater than zero"
                );

        when(validationException.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(
                        nameError,
                        priceError
                ));

        ResponseEntity<ValidationErrorResponse> response =
                exceptionHandler.handleValidationErrors(
                        validationException,
                        request
                );

        assertEquals(400, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                400,
                response.getBody().getStatus()
        );

        assertEquals(
                "Validation Failed",
                response.getBody().getError()
        );

        assertEquals(
                "/api/test",
                response.getBody().getPath()
        );

        assertNotNull(response.getBody().getErrors());

        assertEquals(
                "Resource name is required",
                response.getBody()
                        .getErrors()
                        .get("name")
        );

        assertEquals(
                "Price must be greater than zero",
                response.getBody()
                        .getErrors()
                        .get("price")
        );

        assertNotNull(
                response.getBody().getTimestamp()
        );
    }


    // =========================================================
    // CONSTRAINT VIOLATION
    // =========================================================

    @Test
    void handleConstraintViolation_shouldReturn400() {

        when(constraintViolationException.getMessage())
                .thenReturn("Invalid parameter");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleConstraintViolation(
                        constraintViolationException,
                        request
                );

        assertEquals(400, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                400,
                response.getBody().getStatus()
        );

        assertEquals(
                "Bad Request",
                response.getBody().getError()
        );

        assertEquals(
                "Invalid parameter",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/test",
                response.getBody().getPath()
        );
    }


    // =========================================================
    // GENERIC EXCEPTION
    // =========================================================

    @Test
    void handleGenericException_shouldReturn500() {

        Exception exception =
                new RuntimeException(
                        "Database connection failed"
                );

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleGenericException(
                        exception,
                        request
                );

        assertEquals(500, response.getStatusCode().value());

        assertNotNull(response.getBody());

        assertEquals(
                500,
                response.getBody().getStatus()
        );

        assertEquals(
                "Internal Server Error",
                response.getBody().getError()
        );

        /*
         * Internal exception details must not
         * be exposed to the client.
         */
        assertEquals(
                "An unexpected error occurred",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/test",
                response.getBody().getPath()
        );

        assertNotNull(
                response.getBody().getTimestamp()
        );
    }
}