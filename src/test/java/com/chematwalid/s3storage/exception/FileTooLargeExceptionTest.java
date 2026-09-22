package com.chematwalid.s3storage.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileTooLargeExceptionTest {
    @Test
    void stringConstructorPreservesStorageErrorContract() {
        FileTooLargeException exception = new FileTooLargeException("too large");

        assertThat(exception.getHttpStatusCode()).isEqualTo(400);
        assertThat(exception.getErrorCode()).isEqualTo("FILE_TOO_LARGE");
        assertThat(exception.getMessage()).isEqualTo("too large");
    }
}
