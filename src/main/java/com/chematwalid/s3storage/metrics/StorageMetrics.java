package com.chematwalid.s3storage.metrics;

/** Optional instrumentation boundary; applications may provide their own recorder. */
public interface StorageMetrics {

    void recordVirusScan(String outcome);

    void setDependencyAvailability(String dependency, boolean available);

    static StorageMetrics noop() {
        return new StorageMetrics() {
            @Override public void recordVirusScan(String outcome) { }
            @Override public void setDependencyAvailability(String dependency, boolean available) { }
        };
    }
}
