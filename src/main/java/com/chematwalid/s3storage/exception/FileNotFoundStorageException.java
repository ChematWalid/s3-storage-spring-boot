package com.chematwalid.s3storage.exception;

/**
 * Thrown when a requested storage key does not exist.
 */
public class FileNotFoundStorageException extends StorageException {

    public FileNotFoundStorageException(String key) {
        super(404, StorageErrorCode.FILE_NOT_FOUND, String.format("File not found for key: %s", key));
    }
}
