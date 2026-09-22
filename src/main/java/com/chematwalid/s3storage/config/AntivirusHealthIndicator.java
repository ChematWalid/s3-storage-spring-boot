package com.chematwalid.s3storage.config;

import com.chematwalid.s3storage.metrics.StorageMetrics;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Reports ClamAV reachability for the production upload readiness gate. */
public class AntivirusHealthIndicator implements HealthIndicator {

    private final StorageProperties.VirusScanProperties properties;
    private final StorageMetrics metrics;

    public AntivirusHealthIndicator(StorageProperties storageProperties, StorageMetrics metrics) {
        this.properties = storageProperties.getVirusScan();
        this.metrics = metrics;
    }

    @Override
    public Health health() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(properties.getHost(), properties.getPort()),
                    Math.toIntExact(properties.getConnectionTimeout().toMillis()));
            metrics.setDependencyAvailability("clamav", true);
            return Health.up().build();
        } catch (IOException exception) {
            metrics.setDependencyAvailability("clamav", false);
            return Health.down().build();
        }
    }
}
