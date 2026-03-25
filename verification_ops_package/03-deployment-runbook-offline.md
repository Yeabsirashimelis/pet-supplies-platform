# Deployment Runbook (Offline Intranet)

## 1. Prerequisites
- OS hardening completed (CIS baseline or internal policy).
- Java 17+ installed.
- MySQL 8+ installed and reachable from app host.
- NTP/time sync available inside intranet.
- TLS certificate and keystore provisioned internally.

## 2. Artifacts
- Application jar (monolith).
- `application.yml` per environment.
- Flyway migration scripts bundled in jar.

## 3. Database Preparation
1. Create DB user with least privileges required by app.
2. Create schema `pet_platform`.
3. Ensure MySQL settings:
   - `sql_mode` includes strict mode
   - `innodb_file_per_table=ON`
   - transaction isolation per policy (recommended `READ COMMITTED`)
4. Validate connectivity from app host.

## 4. Configuration
- Set datasource URL/user/password.
- Enable SSL for server in production:
  - `server.ssl.enabled=true`
  - keystore path/password/alias configured
- Set scheduler crons:
  - report: `0 0 2 * * ?`
  - backup full/incremental according to policy
- Ensure message retention = 180 days.

## 5. Deployment Procedure
1. Put current system in maintenance mode (if upgrade).
2. Backup database (pre-deploy checkpoint).
3. Deploy jar and config.
4. Start service.
5. Verify Flyway migration success in startup logs.
6. Execute smoke checks:
   - health endpoint
   - login endpoint
   - one protected endpoint with token
7. Verify bootstrap seed exists (roles and admin).

## 6. Post-Deployment Validation
- Verify scheduler entries generated (`job_execution`, `backup_record`).
- Verify audit logs append for API calls.
- Verify role-based access via sample accounts.
- Verify WebSocket endpoint handshake and message send.

## 7. Rollback Plan
- Stop current service.
- Restore DB to pre-deploy backup.
- Re-deploy previous stable jar/config.
- Run smoke checks and declare rollback complete.

## 8. Operational Monitoring (Offline)
- Application logs (file rotation enabled).
- DB health and slow query logs.
- Scheduler execution success/failure counts.
- Disk capacity for message media, exports, backups.
