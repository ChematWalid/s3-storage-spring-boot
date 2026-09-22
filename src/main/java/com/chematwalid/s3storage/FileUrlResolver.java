package com.chematwalid.s3storage;

import com.chematwalid.s3storage.config.StorageProperties;

/**
 * Central resolver for converting internal opaque storage keys into public or CDN accessible URLs.
 * Encapsulates file-serving prefix formatting and trailing slash normalization.
 */
public class FileUrlResolver {

    private final StorageProperties properties;

    public FileUrlResolver(StorageProperties properties) {
        this.properties = properties;
    }
    /**
     * Resolves an internal storage key to its public URL representation.
     *
     * @param storageKey the raw storage key (e.g. UUID filename)
     * @return the fully qualified or relative public file serving URL, or null if key is null or blank
     */
    public String toUrl(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return null;
        }
        return getPrefix() + storageKey;
    }

    /**
     * Returns the configured file serving prefix normalized with a trailing slash.
     *
     * @return normalized file serving prefix ending with '/'
     */
    public String getPrefix() {
        String prefix = properties.getPublicUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            return "/files/";
        }
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    /**
     * Extracts an opaque storage key from a URL created by this resolver.
     *
     * @param fileUrl relative file-serving URL
     * @return storage key, or {@code null} when the URL is not managed by this resolver
     */
    public String toStorageKey(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        String prefix = getPrefix();
        if (!fileUrl.startsWith(prefix)) {
            return null;
        }
        String storageKey = fileUrl.substring(prefix.length());
        return storageKey.isBlank() || storageKey.contains("/") ? null : storageKey;
    }
}
