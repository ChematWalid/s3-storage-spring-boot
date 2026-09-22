package com.chematwalid.s3storage.exception;

/**
 * Base domain exception for all file storage operations.
 * Encapsulates an HTTP status code (as a plain int) and a machine-readable error code
 * for API error reporting. Using a plain int keeps this library free of a spring-web dependency
 * so consumers can map it to their own framework's status type (Spring MVC HttpStatus,
 * Jakarta REST Response.Status, Quarkus, etc.).
 */
public class StorageException extends RuntimeException {

    private final int httpStatusCode;
    private final String errorCode;

    public StorageException(int httpStatusCode, String errorCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }

    public StorageException(int httpStatusCode, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }

    public StorageException(int httpStatusCode, StorageErrorCode errorCode, String message) {
        this(httpStatusCode, errorCode.value(), message);
    }

    public StorageException(int httpStatusCode, StorageErrorCode errorCode, String message, Throwable cause) {
        this(httpStatusCode, errorCode.value(), message, cause);
    }

    public StorageException(String message) {
        this(500, StorageErrorCode.STORAGE_ERROR, message);
    }

    public StorageException(String message, Throwable cause) {
        this(500, StorageErrorCode.STORAGE_ERROR, message, cause);
    }

    /** HTTP status code (e.g. 400, 404, 500) to be mapped to the consuming framework's type. */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /** Machine-readable storage error code string. */
    public String getErrorCode() {
        return errorCode;
    }
}
