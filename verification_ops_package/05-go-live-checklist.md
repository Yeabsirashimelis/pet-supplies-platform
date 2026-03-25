# Go-Live Checklist and Go/No-Go Criteria

## 1. Engineering Checklist
- [ ] Code freeze completed.
- [ ] All migrations reviewed and approved.
- [ ] Critical/high defects resolved.
- [ ] Security review completed (auth, RBAC, lockout, audit, approval flow).
- [ ] Scheduler config reviewed.

## 2. QA Checklist
- [ ] Regression suite pass.
- [ ] Acceptance criteria pass (GWT set).
- [ ] API contract verification pass.
- [ ] Concurrency tests pass for IM/cooking.
- [ ] Data integrity tests pass.
- [ ] Compliance tests pass (append-only audit, dual approval, desensitized logs).

## 3. Operations Checklist
- [ ] Production host hardened and patched.
- [ ] DB backup checkpoint taken pre-go-live.
- [ ] TLS keystore installed and validated.
- [ ] Monitoring and log rotation configured.
- [ ] Runbook owners on-call and escalation paths ready.

## 4. Go/No-Go Criteria

### Go
- Zero open Critical defects.
- Zero open High defects in auth/security/data integrity/scheduler.
- Full smoke test pass in production-like environment.
- Backup and restore drill completed within SLA.
- Stakeholder sign-off (Engineering, QA, Ops, Security).

### No-Go
- Any unresolved Critical defect.
- Any unresolved High defect affecting lockout, approval, audit immutability, retention, or unique-key integrity.
- Migration instability or rollback uncertainty.
- Missing backup checkpoint or failed restore readiness.

## 5. Day-0 Cutover Steps
1. Announce maintenance window.
2. Take final pre-cutover backup.
3. Deploy and migrate.
4. Execute smoke tests.
5. Enable user traffic.
6. Monitor first 2 hours with war-room.
