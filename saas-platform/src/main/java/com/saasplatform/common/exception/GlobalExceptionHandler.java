package com.saasplatform.common.exception;

import com.saasplatform.common.response.StandardApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.Exception;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleTenantNotFound(TenantNotFoundException ex){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(TenantAlreadyExistsException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleTenantAlreadyExists(TenantAlreadyExistsException ex){
        return  ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(StandardApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(StandardApiResponse.failure("Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardApiResponse<Void>> handleGenericException(Exception ex){
        log.error("Unexpected error occurred", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardApiResponse.failure("An unexpected error occurred. Please try again."));
    }

    @ExceptionHandler(TenantNotActiveException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleTenantNotActiveException(TenantNotActiveException ex){
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(StandardApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleUserNotActiveException(UserNotActiveException ex){
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(StandardApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleUserNotFoundException(UserNotFoundException ex){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(StandardApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<StandardApiResponse<Void>> handleUserAlreadyExistsException(UserAlreadyExistsException ex){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(StandardApiResponse.failure(ex.getMessage()));
    }

}
