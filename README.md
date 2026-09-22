# S3 Storage Spring Boot

Reusable file storage for Spring Boot applications. It supports AWS S3 and
S3-compatible providers such as MinIO and Cloudflare R2, with an in-memory
backend for tests, magic-byte validation, size limits, optional ClamAV
scanning, image variants, transaction-aware cleanup, and Actuator health checks.

This repository is standalone: it has no Souklab domain, authorization,
database, messaging, or application-metrics dependencies.

## Use it

Build and test:

```bash
./mvnw test
```

Add the dependency to a Spring Boot application:

```xml
<dependency>
  <groupId>com.chematwalid</groupId>
  <artifactId>s3-storage-spring-boot</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The starter auto-configures `StorageService`, `FileValidator`, image
processing, and virus scanning. Use `storage.provider=in-memory` for local
tests or `storage.provider=s3` for S3-compatible storage.

```yaml
storage:
  provider: s3
  public-url-prefix: /files/
  validation:
    max-file-size: 10MB
    allowed-mime-types: [image/jpeg, image/png, application/pdf]
  s3:
    endpoint: http://localhost:9000
    region: us-east-1
    bucket: uploads
    access-key: minioadmin
    secret-key: minioadmin-secret
    path-style-access: true
    auto-create-bucket: true
  virus-scan:
    enabled: false
```

The public API is under `com.chematwalid.s3storage`. HTTP authorization and
file-serving routes intentionally remain application concerns; applications
can compose `StorageService` with their own access policy.
