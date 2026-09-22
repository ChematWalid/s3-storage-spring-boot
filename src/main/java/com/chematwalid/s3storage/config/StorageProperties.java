package com.chematwalid.s3storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration properties for the file storage module.
 * Binds to prefix "storage" in application.properties or environment.
 */
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * Storage backend provider name: "in-memory" (test/dev stub), "s3" (MinIO/AWS/R2).
     */
    private String provider;

    /** Optional prefix used by {@link com.chematwalid.s3storage.FileUrlResolver}. */
    private String publicUrlPrefix = "/files/";

    /**
     * Validation rules for uploaded files.
     */
    private ValidationProperties validation = new ValidationProperties();

    /**
     * S3-compatible storage configuration (MinIO, AWS S3, Cloudflare R2).
     */
    private S3Properties s3 = new S3Properties();

    /**
     * Antivirus scanning configuration for ClamAV daemon integration.
     */
    private VirusScanProperties virusScan = new VirusScanProperties();

    /**
     * Rate limiting configuration for file operations.
     */
    private RateLimitProperties rateLimit = new RateLimitProperties();

    // ── top-level getters/setters ────────────────────────────────────────────

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getPublicUrlPrefix() { return publicUrlPrefix; }
    public void setPublicUrlPrefix(String publicUrlPrefix) { this.publicUrlPrefix = publicUrlPrefix; }

    public ValidationProperties getValidation() { return validation; }
    public void setValidation(ValidationProperties validation) { this.validation = validation; }

    public S3Properties getS3() { return s3; }
    public void setS3(S3Properties s3) { this.s3 = s3; }

    public VirusScanProperties getVirusScan() { return virusScan; }
    public void setVirusScan(VirusScanProperties virusScan) { this.virusScan = virusScan; }

    public RateLimitProperties getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimitProperties rateLimit) { this.rateLimit = rateLimit; }

    // ── nested classes ───────────────────────────────────────────────────────

    /**
     * Nested validation properties for uploaded files.
     */
    public static class ValidationProperties {
        /**
         * Maximum allowed file size for uploads (e.g. 10MB).
         */
        private DataSize maxFileSize = DataSize.ofMegabytes(10);

        /**
         * List of allowed MIME types. Note: WebP was deliberately excluded because no pure-Java ImageIO
         * writer exists, and native JNI options were rejected due to portability and container-compatibility concerns.
         */
        private List<String> allowedMimeTypes = new ArrayList<>(Arrays.asList(
                "image/jpeg", "image/png", "application/pdf"));

        public DataSize getMaxFileSize() { return maxFileSize; }
        public void setMaxFileSize(DataSize maxFileSize) { this.maxFileSize = maxFileSize; }

        public List<String> getAllowedMimeTypes() { return allowedMimeTypes; }
        public void setAllowedMimeTypes(List<String> allowedMimeTypes) { this.allowedMimeTypes = allowedMimeTypes; }
    }

    /**
     * Nested S3-compatible configuration properties.
     */
    public static class S3Properties {
        /**
         * S3 endpoint URI (e.g. "http://localhost:9000" for local MinIO; empty or null for AWS S3).
         */
        private String endpoint;

        /**
         * AWS region (e.g. "us-east-1").
         */
        private String region;

        /**
         * Target S3 bucket name.
         */
        private String bucket;

        /**
         * S3 Access Key ID / Root User.
         */
        private String accessKey;

        /**
         * S3 Secret Access Key / Root Password.
         */
        private String secretKey;

        /**
         * Whether to use path-style access (e.g. http://endpoint/bucket/key).
         * Required for MinIO; compatible with AWS S3.
         */
        private Boolean pathStyleAccess;

        /**
         * Automatically create bucket on startup if it doesn't exist.
         */
        private Boolean autoCreateBucket;

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getBucket() { return bucket; }
        public void setBucket(String bucket) { this.bucket = bucket; }

        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }

        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }

        public Boolean getPathStyleAccess() { return pathStyleAccess; }
        public void setPathStyleAccess(Boolean pathStyleAccess) { this.pathStyleAccess = pathStyleAccess; }

        public Boolean getAutoCreateBucket() { return autoCreateBucket; }
        public void setAutoCreateBucket(Boolean autoCreateBucket) { this.autoCreateBucket = autoCreateBucket; }
    }

    /**
     * Nested antivirus scanning configuration properties.
     */
    public static class VirusScanProperties {
        /**
         * Whether antivirus scanning via ClamAV is enabled.
         */
        private boolean enabled;

        /**
         * ClamAV daemon hostname or IP address.
         */
        private String host;

        /**
         * ClamAV daemon TCP port (standard: 3310).
         */
        private int port;

        /**
         * Socket connection timeout.
         */
        private Duration connectionTimeout = Duration.ofSeconds(2);

        /**
         * Socket read timeout for scanning streaming data.
         */
        private Duration readTimeout = Duration.ofSeconds(10);

        /**
         * Failure policy when ClamAV daemon is unreachable or errors:
         * true = fail-open (log warning, allow upload to proceed),
         * false = fail-closed (reject upload with VirusScanException).
         */
        private boolean failOpen;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public Duration getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(Duration connectionTimeout) { this.connectionTimeout = connectionTimeout; }

        public Duration getReadTimeout() { return readTimeout; }
        public void setReadTimeout(Duration readTimeout) { this.readTimeout = readTimeout; }

        public boolean isFailOpen() { return failOpen; }
        public void setFailOpen(boolean failOpen) { this.failOpen = failOpen; }
    }

    /**
     * Nested rate limiting configuration properties for file operations.
     */
    public static class RateLimitProperties {
        /**
         * Whether rate limiting for file operations is enabled.
         */
        private boolean enabled;

        /**
         * Maximum number of requests allowed within the refill duration.
         */
        private int capacity;

        /**
         * Duration over which the rate limit capacity refills (e.g. 1m).
         */
        private Duration refillDuration;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        public Duration getRefillDuration() { return refillDuration; }
        public void setRefillDuration(Duration refillDuration) { this.refillDuration = refillDuration; }
    }
}
