# Test Strategy

## 1. Objectives
- Validate functional correctness across all domains.
- Enforce hard constraints in both code and database.
- Prove security/compliance controls under expected and adversarial scenarios.
- Verify scheduler behavior and retention/backup policies in offline mode.

## 2. Test Pyramid and Ownership
- Unit tests (Engineering): service/domain logic, validators, policy checks.
- Integration tests (Engineering): Spring context + MySQL + Flyway + repos/services.
- API tests (QA + Engineering): endpoint contract, status/error payloads, authorization.
- Non-functional tests (Engineering + QA): concurrency, retention jobs, data race behavior.

## 3. Environments
- `dev-local`: developer machine, disposable DB.
- `test-int`: isolated intranet VM, MySQL 8, no internet.
- `uat-preprod`: production-like, same topology and resource limits.

## 4. Tooling Recommendations
- Unit/Integration: JUnit 5, Spring Boot Test, AssertJ, Testcontainers (if allowed in intranet) or dedicated MySQL test schema.
- API tests: RestAssured/Postman/Newman.
- Concurrency: JUnit + ExecutorService + Awaitility.
- Security checks: scripted negative tests + SQL assertions.
- Scheduler tests: time-manipulated integration tests and manual trigger endpoints.

## 5. Coverage by Category

### 5.1 Unit Tests
- **AuthService**: password regex, failed login count, 15-minute lockout after 5 failures, reset on success.
- **CatalogService**: category depth computation, duplicate product/sku checks.
- **InventoryService**: threshold trigger behavior, negative stock prevention.
- **ImService**: 10s duplicate fold, image MIME/size validation, fingerprint dedup path.
- **AchievementService**: monotonic version increments only.
- **ApprovalService**: initiator cannot self-approve, status transitions.
- **AuditTrailService**: hash chain generation with `hash_prev/hash_self`.

### 5.2 Integration Tests
- Flyway migration success from empty schema.
- JPA mappings and constraints reflect schema (`unique/check/fk`).
- End-to-end login->session->authorized access.
- Controller-service-repo flow for create/update/list actions.
- Scheduled job persistence (`job_execution`, `backup_record`).

### 5.3 API Tests
- Contract verification for all endpoints in `api_contracts.md`.
- Standard error format verification.
- Negative cases: missing fields, invalid values, unauthorized/forbidden.
- Idempotency behavior for mutating APIs using `X-Idempotency-Key` where implemented.

### 5.4 Role-based Authorization Tests
- ADMIN full access.
- MERCHANT owner-scoped operations only.
- BUYER restricted read and self operations.
- REVIEWER reporting/audit/achievement review scope.
- Verify explicit deny cases (403) for out-of-scope resources.

### 5.5 Concurrency Tests
- Parallel timer operations (pause/resume/cancel race).
- IM recall vs read race (message recalled while unread cursor updates).
- Duplicate text messages sent concurrently in same session (fold count stable).
- Inventory concurrent adjustment avoiding negative quantity.

### 5.6 Data Integrity Tests
- DB unique keys:
  - `product.product_code` unique
  - `sku.sku_barcode` unique
- Category depth hard limit <= 4.
- Achievement version uniqueness `(achievement_no, version)` and monotonicity.
- FK integrity across user/session/product/sku/message chains.

### 5.7 Security/Compliance Tests
- Lockout policy (5 failures -> 15 min lock).
- Sensitive fields stored encrypted (`*_enc` fields non-plaintext in DB).
- Logging desensitization (no raw password/token/PII in logs).
- Dual approval required for critical operations.
- Audit append-only enforcement (update/delete blocked by DB triggers).
- TLS configuration validation in deployment (keystore, protocol, cipher policy).

### 5.8 Scheduler and Retention Tests
- Report generation fires daily at 2:00 AM.
- Cooking autosave fires every 30s for running/paused sessions.
- Message cleanup removes data past 180-day retention.
- Backup jobs create daily full + hourly incremental records.
- Backup record expiry at 30 days.

## 6. Test Data Management
- Seed roles and admin account from bootstrap.
- Generate merchant/buyer/reviewer fixtures.
- Dedicated factories for product/sku/category trees and IM sessions.
- Reset schema between suites (Flyway clean+migrate in isolated environment only).

## 7. Entry / Exit Criteria
- Entry: build green, migrations pass, baseline seed available.
- Exit:
  - Critical tests pass 100%
  - High severity defects = 0
  - Medium defects accepted with mitigation and target fix date

## 8. Release Gate Metrics
- API pass rate >= 98%
- Security/compliance pass rate = 100%
- Scheduler reliability in soak test >= 99% successful executions
- No data integrity violations in DB checks
