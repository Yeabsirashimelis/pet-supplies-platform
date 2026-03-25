# Pet Supplies Trading and Cooking Practice Platform API Contracts

Base URL: `/api/v1`

Common headers:
- `Authorization: Bearer <session_token>`
- `X-Request-Id: <uuid>`
- `X-Idempotency-Key: <uuid>` (required on create/mutation endpoints marked idempotent)

Common error format:
```json
{
  "code": "AUTH_INVALID_CREDENTIALS",
  "message": "Username or password is incorrect",
  "requestId": "3bcf2b9f-97e2-4ef0-a9d0-4e3e3974256d",
  "details": []
}
```

Common error codes:
- `AUTH_INVALID_CREDENTIALS`, `AUTH_ACCOUNT_LOCKED`, `AUTH_PASSWORD_WEAK`, `AUTH_SESSION_EXPIRED`
- `PERMISSION_DENIED`, `APPROVAL_REQUIRED`, `APPROVAL_NOT_FOUND`, `APPROVAL_SELF_APPROVAL_FORBIDDEN`
- `VALIDATION_ERROR`, `RESOURCE_NOT_FOUND`, `CONFLICT_DUPLICATE_KEY`, `CONCURRENT_MODIFICATION`
- `FILE_TYPE_NOT_ALLOWED`, `FILE_TOO_LARGE`, `RATE_LIMITED`, `RETENTION_VIOLATION`
- `SYSTEM_ERROR`

---

## Part A - Database Design Notes

### 1) ER model summary by domain

- Auth/Account/Security: `account_user` (1..n) `auth_session`, `login_attempt`, m:n via `user_role`; roles m:n permissions via `role_permission`; policies in `security_policy`; key lifecycle in `encryption_key_meta`.
- Approval/Compliance: `approval_request` (1..n) `approval_action`; references into `audit_log` by `approval_request_id`; immutable append-only `audit_log` with hash chain.
- Catalog/Inventory: `brand`, hierarchical `category`, `product` (SPU), `sku`; flexible attributes via `attribute_definition` + `attribute_option`; value tables for product/sku; `inventory` + append logs in `inventory_log` + `inventory_alert_event`.
- IM/Notification: `im_session` + `im_session_member`; `message` + per-user read cursor `message_read_cursor`; internal notifications in `notification` and preferences in `notification_subscription`.
- Reporting/Scheduler/Backup: `indicator_definition`, `report_metrics`; scheduling in `report_schedule`; runtime executions in `job_execution`; backup history in `backup_record`.
- Cooking/Achievement: `cooking_process`, `cooking_step`, user runtime `cooking_session_progress`, timers in `cooking_timer`; immutable versioned results in `achievement_archive`; attachments in `attachment_version`.

### 2) Table definitions / 3) constraints

All complete table definitions, PK/FK/index/unique/check constraints are in `schema.sql`.

Key hard constraints mapped:
- Unique: `product.product_code`, `sku.sku_barcode`
- Category depth: `category.depth CHECK (1..4)`
- Inventory default threshold: `inventory.alert_threshold DEFAULT 10`
- IM anti-spam support fields: `message.content_hash`, `folded_count`
- Image constraints: `message.image_mime CHECK in jpg/png`, `image_size_bytes <= 2MB`, fingerprint index
- Message retention: `message.expires_at` indexed for purge
- Report schedule default 2AM: `report_schedule.cron_expr` default policy in `security_policy`
- Achievement version monotonic: `achievement_archive.version > 0`, unique `(achievement_no, version)`
- Dual approval: `approval_request`, `approval_action` + trigger preventing self-approval
- Audit immutable: `audit_log` with update/delete blocking triggers
- Backup strategy support: `backup_record` with type/status/retention metadata

### 4) Retention strategy (messages 180 days)

- On insert into `message`, app sets `expires_at = created_at + INTERVAL retention_days DAY` where retention default from `security_policy.IM_MESSAGE_RETENTION_DAYS` (180).
- Scheduled job `RETENTION` runs hourly/daily:
  - soft window check (optional archive table) then hard delete expired rows.
  - cascade/cleanup `message_read_cursor.last_read_message_id` if needed.
- Index `idx_message_retention (expires_at)` enables efficient purge.

### 5) Scheduler-related tables

- `report_schedule`: report cron definition, enabled flag, next run.
- `job_execution`: every scheduled/manual run status and output.
- `backup_record`: full/incremental backup manifests and retention expiry.

---

## Part B - API Contracts

## 1) Auth / Session

### POST `/auth/login`
- Roles allowed: public
- Request:
```json
{ "username": "merchant01", "password": "abc12345" }
```
- Response 200:
```json
{
  "sessionToken": "<opaque-token>",
  "expiresAt": "2026-03-25T09:00:00Z",
  "user": { "id": 1001, "username": "merchant01", "roles": ["MERCHANT"] }
}
```
- Validation: username required; password >=8 and alnum on password-set endpoints; lock account 15 min after 5 failures.
- Errors: `AUTH_INVALID_CREDENTIALS`, `AUTH_ACCOUNT_LOCKED`, `AUTH_ACCOUNT_DISABLED`.
- Idempotency/concurrency: non-idempotent; last successful login updates `last_login_at`.

### POST `/auth/logout`
- Roles: ADMIN/MERCHANT/BUYER/REVIEWER
- Request: `{ "allDevices": false }`
- Response: `{ "revoked": true }`
- Validation: active session required.
- Errors: `AUTH_SESSION_EXPIRED`.
- Notes: idempotent by session token (repeat logout returns revoked true).

### GET `/auth/sessions`
- Roles: all authenticated
- Response:
```json
{ "items": [{ "id": 22, "status": "ACTIVE", "createdAt": "...", "expiresAt": "..." }] }
```

### DELETE `/auth/sessions/{sessionId}`
- Roles: all authenticated (own), ADMIN (any)
- Response: `{ "revoked": true }`
- Errors: `RESOURCE_NOT_FOUND`, `PERMISSION_DENIED`.

### POST `/auth/password/change`
- Roles: all authenticated
- Request:
```json
{ "oldPassword": "abc12345", "newPassword": "newpass123" }
```
- Response: `{ "changed": true, "changedAt": "..." }`
- Validation: new password >=8 with letters+numbers.
- Errors: `AUTH_PASSWORD_WEAK`, `AUTH_INVALID_CREDENTIALS`.
- Notes: idempotency key required.

## 2) Product/SPU/SKU CRUD + list/delist + batch import/export

### POST `/products`
- Roles: MERCHANT, ADMIN
- Request:
```json
{
  "productCode": "P-10001",
  "productName": "Freeze-dried Chicken Cubes",
  "brandId": 11,
  "categoryId": 120,
  "description": "High protein",
  "attributeValues": [{ "attrCode": "origin", "value": "CN" }]
}
```
- Response: `{ "id": 501, "productCode": "P-10001", "status": "DRAFT" }`
- Validation: unique `productCode`; category exists and enabled.
- Errors: `CONFLICT_DUPLICATE_KEY`, `VALIDATION_ERROR`.
- Notes: idempotent by `X-Idempotency-Key`.

### GET `/products/{id}`
- Roles: ADMIN/MERCHANT/BUYER/REVIEWER
- Response includes spu, skus, attributes, inventory summary.

### PUT `/products/{id}`
- Roles: MERCHANT(owner), ADMIN
- Request contains `version` for optimistic lock.
- Errors: `CONCURRENT_MODIFICATION` when version mismatch.

### DELETE `/products/{id}`
- Roles: MERCHANT(owner), ADMIN
- Behavior: logical disable if referenced by orders/messages.

### POST `/products/{id}/list`
- Roles: MERCHANT(owner), ADMIN
- Response: `{ "listStatus": "LISTED" }`

### POST `/products/{id}/delist`
- Roles: MERCHANT(owner), ADMIN
- Response: `{ "listStatus": "DELISTED" }`

### POST `/products/{id}/skus`
- Roles: MERCHANT(owner), ADMIN
- Request:
```json
{
  "skuCode": "SKU-9001",
  "skuBarcode": "6900000011111",
  "skuName": "100g pack",
  "salePrice": 39.9,
  "attributeValues": [{ "attrCode": "weight", "value": "100g" }]
}
```
- Validation: unique `skuBarcode`.
- Errors: `CONFLICT_DUPLICATE_KEY`.

### PUT `/skus/{id}` / GET `/skus/{id}` / DELETE `/skus/{id}`
- Roles: MERCHANT(owner), ADMIN
- Notes: delete is soft disable.

### POST `/products/import`
- Roles: MERCHANT, ADMIN
- Request: multipart CSV/XLSX local upload.
- Response: `{ "jobId": 10009, "accepted": 200, "rejected": 3 }`
- Validation: template columns mandatory; dedup by productCode+skuBarcode.
- Errors: `VALIDATION_ERROR`, `CONFLICT_DUPLICATE_KEY`.
- Notes: idempotent with file fingerprint + idempotency key.

### GET `/products/export`
- Roles: MERCHANT, ADMIN, REVIEWER
- Query: filters/status/date/page.
- Response: file stream or `{ "filePath": "/exports/products_20260324.csv" }`.

## 3) Category / Brand / Attribute config

### POST `/categories`
- Roles: ADMIN, MERCHANT(config-scope)
- Request: `{ "categoryCode":"CAT-FOOD", "categoryName":"Food", "parentId": null }`
- Validation: computed depth <=4.
- Errors: `VALIDATION_ERROR`, `CONFLICT_DUPLICATE_KEY`.

### PUT `/categories/{id}` / GET `/categories/tree` / DELETE `/categories/{id}`
- Roles: ADMIN, MERCHANT(config-scope)
- Delete only when no active products bound.

### POST `/brands` / PUT `/brands/{id}` / GET `/brands`
- Roles: ADMIN, MERCHANT(config-scope)

### POST `/attributes`
- Roles: ADMIN, MERCHANT(config-scope)
- Request includes `valueType` in `TEXT|NUMBER|BOOLEAN|ENUM|DATE`.

### POST `/attributes/{id}/options`
- Roles: ADMIN, MERCHANT(config-scope)
- For ENUM attributes only.

## 4) Inventory alerts

### PUT `/inventory/skus/{skuId}`
- Roles: MERCHANT(owner), ADMIN
- Request: `{ "delta": 30, "changeType": "INBOUND", "referenceType": "PO", "referenceId": "PO-11" }`
- Response: current inventory snapshot.
- Notes: transactional update + `inventory_log` append.

### PUT `/inventory/skus/{skuId}/threshold`
- Roles: MERCHANT(owner), ADMIN
- Request: `{ "alertThreshold": 8 }`
- Validation: >=0 default 10 if unset.

### GET `/inventory/alerts`
- Roles: MERCHANT(owner), ADMIN, REVIEWER
- Response list of open/recovered alerts.

### POST `/inventory/alerts/{id}/handle`
- Roles: MERCHANT(owner), ADMIN
- Request: `{ "action": "IGNORE" }`

## 5) WebSocket handshake/send/recall/read-unread

### GET `/im/ws-ticket`
- Roles: all authenticated
- Response: `{ "wsUrl":"wss://host/ws", "ticket":"...", "expiresAt":"..." }`

### WebSocket `/ws?ticket=...`
- Roles: all authenticated
- Validation: ticket/session active.

### POST `/im/sessions`
- Roles: all authenticated
- Request: `{ "type":"P2P", "memberUserIds":[1001,1002] }`

### POST `/im/sessions/{sessionId}/messages`
- Roles: session members
- Request text:
```json
{ "type": "TEXT", "content": "hello" }
```
- Request image (multipart): `file` + `{ "type":"IMAGE" }`
- Validation: same sender+session duplicate text within 10s folded; image only JPG/PNG <=2MB; fingerprint dedup.
- Response includes `foldedCount`.
- Errors: `RATE_LIMITED`, `FILE_TYPE_NOT_ALLOWED`, `FILE_TOO_LARGE`.
- Notes: idempotency key recommended for retries.

### POST `/im/messages/{messageId}/recall`
- Roles: sender, ADMIN/moderator
- Request: `{ "reason": "wrong info" }`
- Errors: `PERMISSION_DENIED`, `RETENTION_VIOLATION`.

### POST `/im/sessions/{sessionId}/read`
- Roles: session members
- Request: `{ "lastReadMessageId": 99881 }`
- Response: `{ "unreadCount": 0 }`

### GET `/im/sessions/{sessionId}/unread`
- Roles: session members
- Response: `{ "unreadCount": 14 }`

## 6) Notification subscribe/query/read

### PUT `/notifications/subscriptions`
- Roles: all authenticated
- Request:
```json
{ "items": [{ "category": "INVENTORY_ALERT", "enabled": true }] }
```

### GET `/notifications`
- Roles: all authenticated
- Query: `status`, `category`, `page`, `size`

### POST `/notifications/{id}/read`
- Roles: owner, ADMIN
- Response: `{ "status": "READ", "readAt": "..." }`
- Notes: idempotent.

### POST `/notifications/read-all`
- Roles: all authenticated
- Response: `{ "updated": 35 }`

## 7) Reporting (indicator defs, aggregation, drill-down, export, schedule)

### POST `/reports/indicators`
- Roles: ADMIN, REVIEWER
- Request:
```json
{
  "indicatorCode": "INV_LOW_STOCK_CNT",
  "indicatorName": "Low stock SKU count",
  "domain": "INVENTORY",
  "metricType": "COUNT",
  "unit": "sku",
  "expressionSql": "SELECT COUNT(*) FROM inventory WHERE available_qty <= alert_threshold"
}
```

### PUT `/reports/indicators/{id}` / GET `/reports/indicators`
- Roles: ADMIN, REVIEWER

### POST `/reports/aggregate`
- Roles: ADMIN, MERCHANT, REVIEWER
- Request: `{ "date":"2026-03-23", "scope":"MERCHANT", "scopeId":"1001", "indicatorCodes":["INV_LOW_STOCK_CNT"] }`
- Response: metric list.

### POST `/reports/drilldown`
- Roles: ADMIN, MERCHANT(owner scope), REVIEWER
- Request includes `indicatorCode`, `dimensions`, `filters`, `page/size`.

### POST `/reports/export`
- Roles: ADMIN, MERCHANT, REVIEWER
- Response: `{ "filePath":"/exports/reports/rpt_20260324_020000.csv" }`

### POST `/reports/schedules`
- Roles: ADMIN
- Request: `{ "scheduleCode":"DAILY_2AM", "cronExpr":"0 0 2 * * ?", "timezone":"Asia/Shanghai", "enabled":true }`

### PUT `/reports/schedules/{id}` / GET `/reports/schedules` / POST `/reports/schedules/{id}/run`
- Roles: ADMIN (run manual may require dual approval if critical)

### GET `/reports/jobs/{jobExecutionId}`
- Roles: ADMIN, REVIEWER

## 8) Cooking process (process-step-timer-reminder, parallel timers, resume)

### POST `/cooking/processes`
- Roles: ADMIN, MERCHANT, REVIEWER
- Request includes process metadata + ordered steps.

### POST `/cooking/processes/{id}/steps`
- Roles: ADMIN, MERCHANT(owner), REVIEWER
- Request:
```json
{
  "stepNo": 2,
  "stepName": "Simmer",
  "instruction": "Heat for 8 minutes",
  "expectedSeconds": 480,
  "requiresTimer": true,
  "parallelGroupNo": 1
}
```

### POST `/cooking/sessions`
- Roles: BUYER, MERCHANT, REVIEWER, ADMIN
- Request: `{ "processId": 901 }`
- Response: `{ "sessionProgressId": 3001, "status": "RUNNING" }`

### POST `/cooking/sessions/{id}/checkpoint`
- Roles: owner, ADMIN
- Request: `{ "currentStepNo": 3, "progress": {"note":"..."} }`
- Validation: autosave every 30s by server timer; immediate on step change.

### POST `/cooking/sessions/{id}/timers`
- Roles: owner, ADMIN
- Request: `{ "stepId": 9901, "timerName": "Boil", "durationSeconds": 300, "reminderIntervalSeconds": 60 }`
- Supports multiple active timers for parallel groups.

### POST `/cooking/timers/{timerId}/pause|resume|cancel`
- Roles: owner, ADMIN

### POST `/cooking/sessions/{id}/resume`
- Roles: owner, ADMIN
- Response returns latest checkpoint + active timers.

## 9) Achievement reports + attachments + versioning + template export

### POST `/achievements`
- Roles: BUYER(self), MERCHANT, REVIEWER, ADMIN
- Request:
```json
{
  "achievementNo": "ACH-20260324-001",
  "userId": 2001,
  "achievementType": "COOKING_PRACTICE",
  "title": "Knife Skill Level 1",
  "score": 88.5,
  "payload": { "duration": 1200 },
  "baseVersion": 0
}
```
- Response: `{ "id": 555, "achievementNo": "ACH-...", "version": 1 }`
- Validation: version must increment only (`newVersion = latest + 1`).
- Errors: `CONCURRENT_MODIFICATION`, `VALIDATION_ERROR`.

### PUT `/achievements/{achievementNo}`
- Roles: MERCHANT, REVIEWER, ADMIN
- Request includes `expectedVersion`.
- Notes: optimistic lock; rollback forbidden by service rule.

### GET `/achievements` / GET `/achievements/{achievementNo}/versions`
- Roles: owner scope + reviewers/admin.

### POST `/achievements/{achievementNo}/attachments`
- Roles: owner scope + reviewers/admin.
- Request: multipart file + meta.
- Response includes `version` for attachment record.
- Validation: local fingerprint dedup optional.

### GET `/achievements/templates/export`
- Roles: ADMIN, REVIEWER, MERCHANT
- Response: template file path/stream.

## 10) Dual approval flows

### POST `/approvals/requests`
- Roles: ADMIN, MERCHANT, REVIEWER (as initiator per policy)
- Request:
```json
{
  "requestType": "PERMISSION_CHANGE",
  "targetType": "USER_ROLE",
  "targetId": "1001",
  "reason": "Grant report export",
  "payload": { "addRoles": ["REVIEWER"] },
  "requiredApprovals": 1
}
```
- Response: `{ "requestNo": "APR-20260324-001", "status": "PENDING" }`

### GET `/approvals/requests` / GET `/approvals/requests/{id}`
- Roles: ADMIN + authorized approvers

### POST `/approvals/requests/{id}/approve`
- Roles: designated approvers
- Request: `{ "comment": "approved" }`
- Errors: `APPROVAL_SELF_APPROVAL_FORBIDDEN`, `PERMISSION_DENIED`, `CONCURRENT_MODIFICATION`.

### POST `/approvals/requests/{id}/reject`
- Roles: designated approvers

### POST `/approvals/requests/{id}/cancel`
- Roles: initiator, ADMIN

## 11) Audit log query

### GET `/audit/logs`
- Roles: ADMIN, REVIEWER(limited scope)
- Query: `actorUserId`, `actionCode`, `targetType`, `targetId`, `from`, `to`, `page`, `size`
- Response:
```json
{
  "items": [
    {
      "id": 90011,
      "traceId": "a11d...",
      "actorUserId": 1001,
      "actionCode": "PERMISSION_CHANGE_APPROVED",
      "targetType": "USER_ROLE",
      "targetId": "1002",
      "resultCode": "SUCCESS",
      "happenedAt": "2026-03-24T02:00:11Z"
    }
  ],
  "page": 1,
  "size": 20,
  "total": 118
}
```
- Validation: max query window (for example 31 days) to protect DB.
- Errors: `PERMISSION_DENIED`, `VALIDATION_ERROR`.
- Notes: append-only, immutable by DB trigger.

---

## Concurrency + Idempotency policy

- Mutation endpoints require `X-Idempotency-Key`; server stores key + response hash for 24h.
- Optimistic lock on mutable resources using `version` or `updatedAt` precondition; mismatch -> `CONCURRENT_MODIFICATION`.
- Batch import/export and reporting run as jobs; repeated trigger with same idempotency key returns existing `jobExecutionId`.
- Approval decisions enforce unique approver per request and no self-approval.

## WebSocket event contracts (summary)

Inbound events:
- `im.send` `{ "sessionId":1, "type":"TEXT", "content":"hi", "clientMsgId":"c-1" }`
- `im.read` `{ "sessionId":1, "lastReadMessageId":123 }`
- `im.recall` `{ "messageId":123, "reason":"wrong" }`

Outbound events:
- `im.message` `{ "id":123, "sessionId":1, "senderUserId":1001, "type":"TEXT", "content":"hi", "foldedCount":2, "createdAt":"..." }`
- `im.unread` `{ "sessionId":1, "unreadCount":4 }`
- `im.recalled` `{ "messageId":123, "recalledAt":"..." }`
- `system.error` `{ "code":"RATE_LIMITED", "message":"Too many operations" }`
