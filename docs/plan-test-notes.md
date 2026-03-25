# Plan and Test Notes

## Scope of hardening completed
- Env-based secrets for datasource and prod TLS keystore password.
- Production TLS profile via `application-prod.yml`.
- Dual approval critical-op minimum set to 2.
- Notification internal event distribution with delivery count and read timestamps support.
- Product CSV import replaced with row-level parsing/validation/conflict report.
- IM anti-spam/image checks preserved and covered by tests.
- Added `src/test` unit coverage for core acceptance rules.
- `run_tests.sh` hardened for Docker daemon checks and clear non-zero exits.

## Added Unit Test Coverage
- `AuthServiceTest`
  - lockout after 5 failed logins
  - reject login during lock window
- `CatalogServiceTest`
  - category depth <= 4
  - duplicate SKU barcode conflict
- `ImServiceTest`
  - duplicate text fold behavior
  - invalid image MIME rejected
- `ApprovalServiceTest`
  - critical op min approval = 2
  - initiator self-approval rejected

## Remaining verification steps
1. Ensure Docker daemon is running.
2. Execute `./run_tests.sh`.
3. If startup fails, inspect `docker compose logs app` for schema/entity mismatch and patch.
4. Re-run until final pass marker appears.

## Expected pass marker
- `run_tests.sh output showing unit tests + API tests passing`
