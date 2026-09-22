package com.chematwalid.s3storage.lifecycle;

import com.chematwalid.s3storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Coordinates deletion of storage objects with database transaction completion.
 */
public class StorageObjectLifecycle {

    private static final Logger log = LoggerFactory.getLogger(StorageObjectLifecycle.class);
    private final StorageService storageService;

    public StorageObjectLifecycle(StorageService storageService) {
        this.storageService = storageService;
    }
    /**
     * Deletes an object after the current transaction commits, or immediately when no synchronization is active.
     *
     * @param storageKey opaque storage key to delete
     */
    public void deleteAfterCommit(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new StorageDeletionAfterCommit(storageService, storageKey));
        } else {
            delete(storageKey);
        }
    }

    private void delete(String storageKey) {
        try {
            storageService.delete(storageKey);
        } catch (Exception exception) {
            log.error("Failed to delete storage object '{}'", storageKey, exception);
        }
    }
}
