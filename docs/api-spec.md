# API Spec (Implemented Behavior)

This document reflects the current monolith implementation behavior.

## Auth
- `POST /api/v1/auth/login`
  - username/password login
  - lockout: 5 consecutive failures -> 15-minute lock window
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/sessions`
- `DELETE /api/v1/auth/sessions/{sessionId}`
- `POST /api/v1/auth/password/change`

## Catalog
- `POST /api/v1/products`
- `PUT /api/v1/products/{id}`
- `GET /api/v1/products`
- `GET /api/v1/products/{id}`
- `POST /api/v1/products/{id}/list`
- `POST /api/v1/products/{id}/delist`
- `POST /api/v1/products/{id}/skus`
- `GET /api/v1/skus/{id}`
- `PUT /api/v1/skus/{id}`
- `DELETE /api/v1/skus/{id}`
- `POST /api/v1/products/import` (CSV only)
  - Required columns: `productCode,productName,brandId,categoryId,description,skuCode,skuBarcode,salePrice`
  - Row-level validation and row-level result report returned
- `GET /api/v1/products/export`

## Category / Brand / Attribute
- `POST /api/v1/categories`
  - depth hard rule: max 4
- `GET /api/v1/categories/tree`
- `POST /api/v1/brands`
- `GET /api/v1/brands`
- `POST /api/v1/attributes`
- `GET /api/v1/attributes`

## Inventory
- `PUT /api/v1/inventory/skus/{skuId}`
- `PUT /api/v1/inventory/skus/{skuId}/threshold`
- `GET /api/v1/inventory/alerts`
- `POST /api/v1/inventory/alerts/{id}/handle`

## IM
- `GET /api/v1/im/ws-ticket`
- `POST /api/v1/im/sessions`
- `POST /api/v1/im/sessions/{sessionId}/messages`
  - duplicate text fold in same session/sender within 10 seconds
  - image: JPG/PNG only and <=2MB
  - image fingerprint dedup supported
- `POST /api/v1/im/messages/{messageId}/recall`
- `POST /api/v1/im/sessions/{sessionId}/read`
- `GET /api/v1/im/sessions/{sessionId}/unread`
- WebSocket STOMP endpoint: `/ws`, app destination `/app/im.send`, broker `/topic/im`

## Notification
- `PUT /api/v1/notifications/subscriptions`
- `GET /api/v1/notifications`
- `POST /api/v1/notifications/{id}/read`
- `POST /api/v1/notifications/read-all`
- `POST /api/v1/notifications/events/publish`
  - internal distribution with per-user subscription check

## Reporting
- `POST /api/v1/reports/indicators`
- `GET /api/v1/reports/indicators`
- `POST /api/v1/reports/aggregate`
- `POST /api/v1/reports/drilldown`
- `POST /api/v1/reports/export`
- `POST /api/v1/reports/schedules`
- `GET /api/v1/reports/schedules`
- `PUT /api/v1/reports/schedules/{id}`
- `POST /api/v1/reports/schedules/{id}/run`
- `GET /api/v1/reports/jobs/{id}`

## Cooking
- `POST /api/v1/cooking/processes`
- `POST /api/v1/cooking/processes/{id}/steps`
- `POST /api/v1/cooking/sessions`
- `POST /api/v1/cooking/sessions/{id}/checkpoint`
  - immediate checkpoint on step update
- `POST /api/v1/cooking/sessions/{id}/timers`
- `POST /api/v1/cooking/timers/{id}/pause`
- `POST /api/v1/cooking/timers/{id}/resume`
- `POST /api/v1/cooking/timers/{id}/cancel`
- `POST /api/v1/cooking/sessions/{id}/resume`
- scheduler autosave every 30s for running/paused sessions

## Achievement
- `POST /api/v1/achievements`
- `PUT /api/v1/achievements/{achievementNo}`
  - version must monotonically increase; mismatch rejected
- `GET /api/v1/achievements`
- `GET /api/v1/achievements/{achievementNo}/versions`
- `POST /api/v1/achievements/{achievementNo}/attachments`
- `GET /api/v1/achievements/templates/export`

## Approval
- `POST /api/v1/approvals/requests`
  - critical types enforce minimum required approvals = 2
- `GET /api/v1/approvals/requests`
- `GET /api/v1/approvals/requests/{id}`
- `POST /api/v1/approvals/requests/{id}/approve`
- `POST /api/v1/approvals/requests/{id}/reject`
- `POST /api/v1/approvals/requests/{id}/cancel`
- initiator self-approval forbidden

## Audit
- `GET /api/v1/audit/logs`
- append-only immutable DB trigger protection
- hash chain fields maintained on append
