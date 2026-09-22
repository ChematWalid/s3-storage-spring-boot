package com.chematwalid.s3storage.config;

import com.chematwalid.s3storage.metrics.StorageMetrics;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

/** Reports S3 availability so production readiness probes do not hide storage outages. */
public class StorageHealthIndicator implements HealthIndicator {

    private final StorageProperties properties;
    private final S3Client s3Client;
    private final StorageMetrics metrics;

    public StorageHealthIndicator(StorageProperties properties, S3Client s3Client, StorageMetrics metrics) {
        this.properties = properties;
        this.s3Client = s3Client;
        this.metrics = metrics;
    }

    @Override
    public Health health() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.getS3().getBucket()).build());
            metrics.setDependencyAvailability("object-storage", true);
            return Health.up().build();
        } catch (RuntimeException exception) {
            metrics.setDependencyAvailability("object-storage", false);
            return Health.down().withDetail("provider", "s3").build();
        }
    }
}
