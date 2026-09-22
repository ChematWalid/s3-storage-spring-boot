package com.chematwalid.s3storage;

/** Stable route contract shared by file-serving adapters and application layers. */
public final class FileServingRoutes {

    public static final String BASE_PATH = "/api/v1/files";
    public static final String DEFAULT_PREFIX = BASE_PATH + "/";

    private FileServingRoutes() {
    }
}
