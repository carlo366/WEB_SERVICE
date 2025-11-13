package com.example.web_service.util;

import com.example.web_service.dto.Response;
import com.example.web_service.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GeneralExceptionHandler {

    // 1. Validation Exception (Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();

        Map<String, String> errors = result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null 
                            ? fieldError.getDefaultMessage() 
                            : "Invalid value",
                        (existing, replacement) -> existing // Handle duplicate keys
                ));

        log.warn("Validation failed: {}", errors);
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.failedResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation failed",
                    errors
                ));
    }

    // 2. Custom Application Exception
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Object> handleApplicationException(ApplicationException ex) {
        log.error("Application exception: {}", ex.getMessage());
        
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(Response.failedResponse(
                    ex.getHttpStatus().value(), 
                    ex.getMessage()
                ));
    }

    // 3. Authentication Exception
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication failed: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Response.failedResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Authentication failed. Please check your credentials."
                ));
    }

    // 4. Bad Credentials Exception
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Bad credentials: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Response.failedResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Invalid username or password"
                ));
    }

    // 5. Access Denied Exception (Security)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Response.failedResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "Access denied. You don't have permission to access this resource."
                ));
    }

    // 6. Data Integrity Violation (Unique constraint, FK violation, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data integrity violation";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique") || ex.getMessage().contains("duplicate")) {
                message = "Record already exists. Please use unique values.";
            } else if (ex.getMessage().contains("foreign key")) {
                message = "Cannot perform operation due to data dependencies.";
            }
        }
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Response.failedResponse(
                    HttpStatus.CONFLICT.value(),
                    message
                ));
    }

    // 7. Illegal Argument Exception
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.failedResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided"
                ));
    }

    // 8. Illegal State Exception
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Response.failedResponse(
                    HttpStatus.CONFLICT.value(),
                    ex.getMessage() != null ? ex.getMessage() : "Invalid state for this operation"
                ));
    }

    // 9. Null Pointer Exception
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Object> handleNullPointerException(NullPointerException ex) {
        log.error("Null pointer exception: ", ex);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.failedResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "An unexpected error occurred. Please try again later."
                ));
    }

    // 10. HTTP Message Not Readable (Invalid JSON)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Invalid request body: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.failedResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid request body. Please check your JSON format."
                ));
    }

    // 11. HTTP Media Type Not Supported
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.error("Unsupported media type: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(Response.failedResponse(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                    "Unsupported media type. Please use application/json."
                ));
    }

    // 12. HTTP Request Method Not Supported
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        log.error("Method not supported: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Response.failedResponse(
                    HttpStatus.METHOD_NOT_ALLOWED.value(),
                    "HTTP method " + ex.getMethod() + " is not supported for this endpoint."
                ));
    }

    // 13. Missing Request Parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex) {
        log.error("Missing request parameter: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.failedResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Missing required parameter: " + ex.getParameterName()
                ));
    }

    // 14. Method Argument Type Mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch: {}", ex.getMessage());
        
        String message = String.format("Invalid value for parameter '%s'. Expected type: %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Response.failedResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    message
                ));
    }

    // 15. Max Upload Size Exceeded
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.error("Max upload size exceeded: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Response.failedResponse(
                    HttpStatus.PAYLOAD_TOO_LARGE.value(),
                    "File size exceeds the maximum allowed size (10MB)."
                ));
    }

    // 16. No Handler Found (404)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.error("No handler found: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Response.failedResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "The requested endpoint does not exist."
                ));
    }

    // 17. Unknown Host Exception
    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity<Object> handleUnknownHost(UnknownHostException ex) {
        log.error("Unknown host: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Response.failedResponse(
                    HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "Unable to connect to external service. Please try again later."
                ));
    }

    // 18. General Exception Handler (Catch-all)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        log.error("Unexpected error occurred: ", ex);

        // Don't expose internal error details in production
        String message = "An unexpected error occurred. Please try again later.";
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.failedResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message
                ));
    }
}

