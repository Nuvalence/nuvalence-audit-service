package io.nuvalence.platform.audit.service.error;

import org.springframework.http.HttpStatus;

/**
 * Custom runtime exception used for signalling error response.
 */
@SuppressWarnings("serial")
public class ApiException extends RuntimeException {
    /**
     * Builds ApiException objects.
     */
    public static class Builder {
        public static ApiException badRequest(String message) {
            return new ApiException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private final HttpStatus httpStatus;

    private ApiException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
