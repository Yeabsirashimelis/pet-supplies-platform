# Acceptance Criteria (Given-When-Then)

## A. Authentication & Session
- Given an active account with correct credentials, When login is requested, Then a valid session token is issued with expiry.
- Given 5 consecutive failed login attempts, When the 6th attempt occurs within lock window, Then login is denied with account-locked error for 15 minutes.
- Given a logged-in user, When logout is called, Then session status is revoked and protected endpoints reject the token.

## B. Product/Category/SKU/Inventory
- Given a new product with unique `product_code`, When created, Then product is persisted in draft/delisted state.
- Given duplicate `product_code`, When create is attempted, Then request fails with duplicate key conflict.
- Given category hierarchy depth 4, When adding child level 5, Then request is rejected.
- Given a SKU with unique barcode, When created, Then SKU is persisted.
- Given duplicate `sku_barcode`, When create is attempted, Then request is rejected.
- Given inventory threshold default, When stock goes <= threshold, Then an inventory alert event is created.

## C. Instant Messaging
- Given a session member sends identical text twice within 10 seconds, When second message arrives, Then content is folded and `folded_count` increments.
- Given an image message with non-JPG/PNG or >2MB, When send is attempted, Then request is rejected.
- Given matching image fingerprint already stored, When image message is sent, Then media is deduplicated by fingerprint path.
- Given sender recalls a message, When recall succeeds, Then message is marked recalled and visible as recalled state.

## D. Notification
- Given user subscription enabled for category, When matching event occurs, Then internal notification is created for that user.
- Given unread notifications, When read-all is requested, Then statuses become READ and timestamps are set.

## E. Reporting Center
- Given enabled schedule with default cron, When scheduler reaches 2:00 AM, Then report job execution record is created and marked success/failure.
- Given indicator definitions exist, When aggregate endpoint is called, Then metrics are returned for requested scope/date.
- Given manual run is requested by admin, When executed, Then job execution record is created with MANUAL trigger.

## F. Cooking Assistance
- Given a running cooking session, When 30 seconds pass, Then progress checkpoint timestamp updates automatically.
- Given step change is submitted, When checkpoint endpoint is called, Then progress is persisted immediately.
- Given parallel timers, When pause/resume/cancel called concurrently, Then timer state transitions remain consistent.

## G. Practice Achievement
- Given no prior version, When first achievement saved, Then version=1 is created.
- Given latest version N, When update with expectedVersion=N, Then new version N+1 is persisted.
- Given expectedVersion mismatch, When update requested, Then request is rejected with concurrency error.

## H. Security & Compliance
- Given critical operation requires approval, When initiator submits request, Then independent approver is required before final approval.
- Given initiator attempts self-approval, When action is submitted, Then operation is rejected.
- Given audit log row exists, When update/delete attempted, Then DB blocks operation (append-only).
- Given sensitive field persistence, When queried raw in DB, Then data is encrypted/ciphertext form.

## I. Scheduler & Retention
- Given messages older than retention window, When retention job runs, Then expired messages are deleted.
- Given backup policy active, When day boundary/hour boundary reached, Then full/incremental backup records are created.
- Given backup records exceed 30-day retention, When cleanup runs, Then expired records are marked expired/removed per policy.
