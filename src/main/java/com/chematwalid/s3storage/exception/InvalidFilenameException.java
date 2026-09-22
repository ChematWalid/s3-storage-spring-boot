package com.chematwalid.s3storage.exception;

/**
 * Thrown when an uploaded filename contains path traversal sequences or illegal control characters.
 */
public class InvalidFilenameException extends StorageException {

    public InvalidFilenameException(String message) {
        super(400, StorageErrorCode.INVALID_FILENAME, message);
    }
}
