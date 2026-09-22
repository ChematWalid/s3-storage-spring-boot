package com.chematwalid.s3storage.exception;

/**
 * Thrown when an image processing operation is attempted on an unsupported or non-image content type,
 * or when image decoding fails.
 */
public class UnsupportedImageFormatException extends StorageException {

    /**
     * Constructs an exception for an unsupported image MIME type.
     *
     * @param contentType the unsupported content type
     */
    public UnsupportedImageFormatException(String contentType) {
        super(400, StorageErrorCode.UNSUPPORTED_IMAGE_FORMAT,
                String.format("Content type '%s' is not supported for image processing", contentType));
    }

    /**
     * Constructs an exception with a custom message and cause.
     *
     * @param message failure detail message
     * @param cause   underlying cause
     */
    public UnsupportedImageFormatException(String message, Throwable cause) {
        super(400, StorageErrorCode.UNSUPPORTED_IMAGE_FORMAT, message, cause);
    }
}
