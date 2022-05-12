package io.nuvalence.platform.audit.service.controllers;

import io.nuvalence.platform.audit.service.error.ApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

/**
 * Returns error response, if exception is thrown in the code.
 */
@ControllerAdvice
public class GlobalErrorHandler {
    /**
     * Error response object.
     */
    @AllArgsConstructor
    @Getter
    public class ErrorResponse {
        private List<String> messages;

        public ErrorResponse(String message) {
            this.messages = Collections.singletonList(message);
        }
    }

    /**
     * Return Bad request if ApiException is thrown in the code.
     * @param e exception
     * @return ResponseEntity with http status defined in the exception
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleException(ApiException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
    }

    /**
     * Return Bad request if MethodArgumentNotValidException is thrown in the code.
     * @param e exception
     * @return ResponseEntity for HTTP 400
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleException(ConstraintViolationException e) {
        return ResponseEntity.badRequest()
                .body(
                        e.getConstraintViolations().isEmpty()
                                ? new ErrorResponse(e.getMessage())
                                : new ErrorResponse(
                                        e.getConstraintViolations().stream()
                                                .map(
                                                        violation ->
                                                                String.format(
                                                                        "'%s': %s",
                                                                        violation.getPropertyPath(),
                                                                        violation.getMessage()))
                                                .collect(Collectors.toList())));
    }

    /**
     * Return Bad request if MethodArgumentNotValidException is thrown in the code.
     * @param e exception
     * @return ResponseEntity for HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body(
                        e.getFieldErrorCount() == 0
                                ? new ErrorResponse(e.getMessage())
                                : new ErrorResponse(
                                        e.getFieldErrors().stream()
                                                .map(
                                                        fieldError ->
                                                                String.format(
                                                                        "'%s': %s",
                                                                        fieldError.getField(),
                                                                        fieldError
                                                                                .getDefaultMessage()))
                                                .collect(Collectors.toList())));
    }
}
