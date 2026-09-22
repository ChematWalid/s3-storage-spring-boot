package com.chematwalid.s3storage.stub;

import java.time.Instant;

/** Immutable in-memory representation of a stored file. */
record StoredFile(byte[] data, String originalFilename, String contentType, Instant storedAt) {
}
