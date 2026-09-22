package com.chematwalid.s3storage.lifecycle;

import com.chematwalid.s3storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;

/** Deletes one storage object after the surrounding transaction commits. */
final class StorageDeletionAfterCommit implements TransactionSynchronization {

    private static final Logger log = LoggerFactory.getLogger(StorageDeletionAfterCommit.class);
    private final StorageService storageService;
    private final String storageKey;

    StorageDeletionAfterCommit(StorageService storageService, String storageKey) {
        this.storageService = storageService;
        this.storageKey = storageKey;
    }

    @Override
    public void afterCommit() {
        try {
            storageService.delete(storageKey);
        } catch (Exception exception) {
            log.error("Failed to delete storage object '{}'", storageKey, exception);
        }
    }
}
