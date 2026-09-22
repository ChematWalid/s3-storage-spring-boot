package com.chematwalid.s3storage.exception;

/** Stable error identifiers for adapters that expose storage failures over HTTP. */
public enum StorageErrorCode {
    STORAGE_ERROR,
    FILE_NOT_FOUND,
    FILE_TOO_LARGE,
    INVALID_FILENAME,
    UNSUPPORTED_FILE_TYPE,
    UNSUPPORTED_IMAGE_FORMAT;

    public String value() {
        return name();
    }
}
