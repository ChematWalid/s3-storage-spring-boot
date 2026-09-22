package com.chematwalid.s3storage.exception;

/**
 * Thrown when an uploaded file exceeds the configured maximum allowed size limit.
 */
public class FileTooLargeException extends StorageException {

    public FileTooLargeException(long actualSize, long maxSize) {
        super(400, StorageErrorCode.FILE_TOO_LARGE,
                String.format("File size (%d bytes) exceeds the maximum allowed limit of %d bytes", actualSize, maxSize));
    }

    public FileTooLargeException(String message) {
        super(400, StorageErrorCode.FILE_TOO_LARGE, message);
    }
}
