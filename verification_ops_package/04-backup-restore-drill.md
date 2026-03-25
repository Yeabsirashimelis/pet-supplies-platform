# Backup and Restore Drill Runbook

## 1. Policy Baseline
- Full backup: daily.
- Incremental backup: hourly.
- Retention: 30 days.
- Backup metadata tracked in `backup_record`.

## 2. Roles and Responsibilities
- Operations: execute backup/restore and maintain storage.
- DBA: validate consistency and integrity.
- App owner: application stop/start and functional validation.

## 3. Backup Drill (Monthly)

### 3.1 Full Backup Drill
1. Trigger full backup job (manual or scheduled validation window).
2. Verify `backup_record` entry:
   - `backup_type=FULL`
   - `status=SUCCESS`
   - checksum populated (if configured)
3. Verify backup file exists and is readable.
4. Verify retention date set to +30 days.

### 3.2 Incremental Backup Drill
1. Trigger incremental backup job.
2. Verify record with `backup_type=INCREMENTAL` and success status.
3. Confirm chain continuity against latest full backup.

## 4. Restore Drill (Quarterly)

### 4.1 Point-In-Time Restore Scenario
Given target time T:
1. Provision isolated restore environment.
2. Restore latest full backup before T.
3. Apply incremental backups up to T.
4. Run DB integrity checks:
   - row counts for key tables
   - FK consistency checks
   - unique key validation for product_code/sku_barcode
5. Start application against restored DB.
6. Execute smoke API tests.

### 4.2 Acceptance for Restore Drill
- RTO within target (define by business, e.g. <= 2 hours).
- RPO within target (e.g. <= 1 hour).
- No integrity violations found.
- Core business flows validated.

## 5. Retention and Cleanup Validation
1. Query records older than 30 days.
2. Validate cleanup marks or deletes expired backups per policy.
3. Ensure no active chain required for restore is deleted.

## 6. Drill Evidence Checklist
- Backup/restore logs
- `backup_record` snapshots
- Integrity SQL output
- Smoke test report
- Sign-off from Ops + DBA + App owner
