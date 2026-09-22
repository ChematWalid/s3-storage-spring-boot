# s3-storage-spring-boot

A drop-in Spring Boot auto-configuration library for S3-compatible file storage.
Add the dependency, set a few properties, and get a fully-wired `StorageService` with
magic-byte MIME validation, size limits, optional ClamAV virus scanning,
image variant generation, transaction-aware deletion, and Actuator health indicators.

Supports **AWS S3**, **MinIO**, **Cloudflare R2**, and any S3-compatible provider.
Uses an **in-memory stub** for tests and local development.

---

## Requirements

| | |
|---|---|
| Java | 21+ |
| Spring Boot | 4.x (Boot 3.x should work but is not tested) |
| Build tool | Maven (wrapper included) |

---

## Installation

Install to your local Maven repository:

```bash
./mvnw install -DskipTests
```

Then add the dependency to your consuming application's `pom.xml`:

```xml
<dependency>
  <groupId>com.chematwalid</groupId>
  <artifactId>s3-storage-spring-boot</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The library uses Spring Boot's auto-configuration mechanism — no `@Import` or
`@ComponentScan` required in your application.

---

## Quick Start

### In-memory (tests / local dev)

```yaml
# application.yml
storage:
  provider: in-memory
  validation:
    max-file-size: 10MB
    allowed-mime-types:
      - image/jpeg
      - image/png
      - application/pdf
```

### S3 / MinIO (production)

```yaml
storage:
  provider: s3
  public-url-prefix: /files/         # prefix used by FileUrlResolver
  validation:
    max-file-size: 10MB
    allowed-mime-types:
      - image/jpeg
      - image/png
      - application/pdf
  s3:
    endpoint: http://localhost:9000   # omit for AWS S3
    region: us-east-1
    bucket: uploads
    access-key: minioadmin
    secret-key: minioadmin-secret
    path-style-access: true           # required for MinIO
    auto-create-bucket: true          # create bucket on startup if missing
```

---

## Configuration Reference

All properties are under the `storage.*` prefix.

### Top-level

| Property | Default | Description |
|---|---|---|
| `storage.provider` | — | **Required.** `in-memory` or `s3` |
| `storage.public-url-prefix` | `/files/` | URL prefix used by `FileUrlResolver` |

### `storage.validation`

| Property | Default | Description |
|---|---|---|
| `max-file-size` | `10MB` | Maximum upload size. Streams are hard-capped — no over-read into memory. |
| `allowed-mime-types` | `image/jpeg, image/png, application/pdf` | Allowed content types. Type is detected via Apache Tika magic-byte sniffing, not the declared `Content-Type` header. |

### `storage.s3`

| Property | Default | Description |
|---|---|---|
| `endpoint` | _(AWS default)_ | Custom S3 endpoint URI. Required for MinIO / R2. |
| `region` | — | AWS region (e.g. `us-east-1`). |
| `bucket` | — | Target bucket name. |
| `access-key` | — | S3 access key ID. |
| `secret-key` | — | S3 secret access key. |
| `path-style-access` | `false` | Use path-style URLs (`endpoint/bucket/key`). Required for MinIO. |
| `auto-create-bucket` | `false` | Create the bucket on startup if it doesn't exist. |

### `storage.virus-scan` _(optional — requires `spring-tx` on classpath)_

| Property | Default | Description |
|---|---|---|
| `enabled` | `false` | Enable ClamAV daemon scanning before every upload. |
| `host` | — | ClamAV daemon hostname or IP. |
| `port` | — | ClamAV daemon port (standard: `3310`). |
| `connection-timeout` | `2s` | Socket connection timeout. |
| `read-timeout` | `10s` | Socket read timeout for streaming data. |
| `fail-open` | `false` | `true` = log warning and allow upload if ClamAV is unreachable. `false` = reject upload. |

### `storage.rate-limit`

| Property | Default | Description |
|---|---|---|
| `enabled` | `false` | Enable rate limiting for file operations. |
| `capacity` | — | Max requests within the refill window. |
| `refill-duration` | — | Duration over which capacity refills (e.g. `1m`). |

---

## Core API

All beans are registered by the auto-configuration and injected via standard Spring DI.

### `StorageService`

The primary interface for storing, retrieving, and deleting files.

```java
@Autowired StorageService storageService;

// Upload
StorageResult result = storageService.store(inputStream, originalFilename, contentType, sizeBytes);
// result.key()              → opaque UUID-based storage key
// result.originalFilename() → sanitized original filename (metadata only)
// result.contentType()      → verified MIME type
// result.size()             → size in bytes
// result.storedAt()         → upload timestamp

// Download
StorageResource resource = storageService.retrieve(key);
// resource.content()        → InputStream

// Delete
storageService.delete(key);

// Existence check
boolean exists = storageService.exists(key);
```

> **Storage keys are always opaque UUID-based identifiers** — never derived from the
> original filename. Filenames are stored as metadata only and are never used as
> path segments.

### `FileValidator`

Validates uploads before they reach the storage backend. Call this before `storageService.store()`.

```java
@Autowired FileValidator fileValidator;

ValidatedFile validated = fileValidator.validate(inputStream, originalFilename, contentType, sizeBytes);
StorageResult result = storageService.store(
    validated.content(), validated.originalFilename(), validated.contentType(), validated.size());
```

`FileValidator` performs:
- **Size check** — rejects if declared size exceeds `max-file-size`
- **Magic-byte MIME sniffing** — uses Apache Tika, ignores the declared `Content-Type`
- **Spoof detection** — rejects if declared type ≠ sniffed type (even if both are allowed)
- **Filename sanitization** — strips path traversal sequences and illegal control characters
- **Stream hard-cap** — wraps the stream in a `SizeLimitingInputStream`; reading stops the moment the actual byte count exceeds the limit, regardless of the declared size

### `FileUrlResolver`

Converts an opaque storage key into a public URL using `storage.public-url-prefix`.

```java
@Autowired FileUrlResolver fileUrlResolver;

String url = fileUrlResolver.resolve("a1b2c3.jpg");  // → "/files/a1b2c3.jpg"
```

### `ImageProcessingService`

Resizes images into multiple resolution tiers without upscaling. Returns one
`ImageVariant` per `ResolutionTier`.

```java
@Autowired ImageProcessingService imageProcessingService;

Map<ResolutionTier, ImageVariant> variants = imageProcessingService.process(inputStream, contentType);
// ResolutionTier.THUMBNAIL → max 150px on longest side
// ResolutionTier.MEDIUM    → max 500px on longest side
// ResolutionTier.ORIGINAL  → max 2000px on longest side

ImageVariant variant = variants.get(ResolutionTier.THUMBNAIL);
// variant.content()     → byte[] of the resized image
// variant.contentType() → MIME type (format preserved from source)
// variant.size()        → byte count
```

Supported formats: JPEG, PNG. WebP is not supported (no pure-Java ImageIO writer).

### `StorageObjectLifecycle` _(requires `spring-tx` on classpath)_

Ties file deletion to database transaction commit. If a transaction is active, the
delete is deferred until after `COMMIT`. If no transaction is active, it deletes
immediately.

```java
@Autowired StorageObjectLifecycle storageObjectLifecycle;

// Inside a @Transactional method — deletion only happens after the DB commit succeeds
storageObjectLifecycle.deleteAfterCommit(oldAvatarKey);
```

---

## Exception Handling

All storage exceptions extend `StorageException` which carries:
- `getHttpStatusCode()` → plain `int` (400, 404, 500) — map to your framework's type
- `getErrorCode()` → machine-readable string from `StorageErrorCode`
- `getMessage()` → human-readable detail

| Exception | HTTP code | Error code |
|---|---|---|
| `FileTooLargeException` | 400 | `FILE_TOO_LARGE` |
| `InvalidFilenameException` | 400 | `INVALID_FILENAME` |
| `UnsupportedFileTypeException` | 400 | `UNSUPPORTED_FILE_TYPE` |
| `UnsupportedImageFormatException` | 400 | `UNSUPPORTED_IMAGE_FORMAT` |
| `FileNotFoundStorageException` | 404 | `FILE_NOT_FOUND` |
| `VirusDetectedException` | 500 | `STORAGE_ERROR` |
| `VirusScanException` | 500 | `STORAGE_ERROR` |
| `StorageException` (base) | 500 | `STORAGE_ERROR` |

Example Spring MVC exception handler:

```java
@ExceptionHandler(StorageException.class)
public ResponseEntity<ProblemDetail> handleStorage(StorageException ex) {
    ProblemDetail problem = ProblemDetail.forStatus(ex.getHttpStatusCode());
    problem.setTitle(ex.getErrorCode());
    problem.setDetail(ex.getMessage());
    return ResponseEntity.status(ex.getHttpStatusCode()).body(problem);
}
```

---

## Health Indicators

The library registers Actuator health indicators automatically when enabled.

| Indicator | Condition | Checks |
|---|---|---|
| `storageHealthIndicator` | `storage.provider=s3` | `HeadBucket` against the configured S3 bucket |
| `antivirusHealthIndicator` | `storage.virus-scan.enabled=true` | TCP socket connect to ClamAV |

Both indicators are skipped when their respective conditions are not met, so
`in-memory` deployments have no unnecessary health dependencies.

---

## Optional Dependencies

The library declares several dependencies as `<optional>true</optional>` to keep
the classpath lean for consumers that don't need them.

| Dependency | When needed |
|---|---|
| `spring-tx` | `StorageObjectLifecycle` / `StorageDeletionAfterCommit` |
| `jackson-annotations` | `EnumValue` serialization with `@JsonValue` |

---

## Security Notes

- **Filename sanitization is metadata-only.** Filenames are stored as human-readable
  metadata but are never used as storage path segments. All keys are UUID-based.
- **MIME sniffing beats the declared header.** A PNG file uploaded with `Content-Type: image/jpeg`
  will be rejected even if both types are in the allowed list.
- **Streams are hard-capped.** A client that declares 100 bytes but sends 500 MB will
  be rejected as soon as the cap is hit — no full read ever occurs.
- **`spring-web` is not required.** Exception status codes use plain `int` so the
  library works with any web framework (Spring MVC, Spring WebFlux, Quarkus, etc.).

---

## Running Tests

```bash
./mvnw test
```

83 tests cover: file validation, MIME spoof detection, size limiting, path traversal
sanitization, S3 service unit tests (with mock S3 client), image resizing (JPEG/PNG,
multiple tiers, no-upscaling policy, extreme aspect ratios), ClamAV scanner, transaction
lifecycle, and auto-configuration.

ClamAV integration tests are designed to be skipped gracefully when no daemon is running.

---

## Project Structure

```
src/main/java/com/chematwalid/s3storage/
├── StorageService.java             # Core upload/retrieve/delete/exists interface
├── StorageResult.java              # Returned after a successful store()
├── StorageResource.java            # Returned by retrieve()
├── FileUrlResolver.java            # Key → public URL
├── FileServingRoutes.java          # Route constants (optional, for consumers)
├── config/
│   ├── StorageConfiguration.java   # Auto-configuration entry point
│   ├── StorageProperties.java      # All storage.* properties
│   ├── StorageHealthIndicator.java # S3 Actuator health
│   └── AntivirusHealthIndicator.java
├── exception/
│   ├── StorageException.java       # Base exception (int httpStatusCode + errorCode)
│   ├── StorageErrorCode.java       # Machine-readable error identifiers
│   └── ...                         # Typed subclasses
├── validation/
│   ├── FileValidator.java          # Magic-byte validation + filename sanitization
│   ├── ValidatedFile.java          # Result of validation
│   └── SizeLimitingInputStream.java
├── s3/
│   └── S3StorageService.java       # S3-compatible backend
├── stub/
│   └── InMemoryStorageService.java # In-memory backend (dev/test)
├── image/
│   ├── ImageProcessingService.java
│   ├── ThumbnailatorImageProcessingService.java
│   ├── ResolutionTier.java         # THUMBNAIL / MEDIUM / ORIGINAL
│   └── ImageVariant.java
├── lifecycle/
│   ├── StorageObjectLifecycle.java # Transaction-aware deletion
│   └── StorageDeletionAfterCommit.java
├── scan/
│   ├── VirusScanService.java       # Antivirus scan orchestration
│   ├── ClamdInstreamScanner.java   # ClamAV INSTREAM protocol
│   ├── VirusScanner.java           # Scanner interface
│   └── ScanResult.java
└── metrics/
    └── StorageMetrics.java         # Optional metrics hook (noop by default)
```

---

## License

MIT
