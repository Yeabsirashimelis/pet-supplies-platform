CREATE TABLE role (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  built_in TINYINT(1) NOT NULL DEFAULT 1,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_role_code (code),
  CONSTRAINT chk_role_status CHECK (status IN ('ACTIVE','DISABLED'))
);

CREATE TABLE permission (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  domain VARCHAR(64) NOT NULL,
  critical_op TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_permission_code (code),
  KEY idx_permission_domain (domain)
);

CREATE TABLE role_permission (
  role_id BIGINT UNSIGNED NOT NULL,
  permission_id BIGINT UNSIGNED NOT NULL,
  granted_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  granted_by BIGINT UNSIGNED NULL,
  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES role(id),
  CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES permission(id)
);

CREATE TABLE account_user (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  password_algo VARCHAR(32) NOT NULL DEFAULT 'BCRYPT',
  password_changed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  display_name VARCHAR(128) NOT NULL,
  phone_enc VARBINARY(512) NULL,
  email_enc VARBINARY(512) NULL,
  failed_login_count INT NOT NULL DEFAULT 0,
  lock_until DATETIME(3) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  last_login_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_account_user_username (username),
  KEY idx_account_user_lock_until (lock_until),
  CONSTRAINT chk_account_user_status CHECK (status IN ('ACTIVE','DISABLED','LOCKED'))
);

CREATE TABLE user_role (
  user_id BIGINT UNSIGNED NOT NULL,
  role_id BIGINT UNSIGNED NOT NULL,
  assigned_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  assigned_by BIGINT UNSIGNED NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES account_user(id),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id)
);

CREATE TABLE password_history (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  changed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_password_history_user FOREIGN KEY (user_id) REFERENCES account_user(id),
  KEY idx_password_history_user_changed (user_id, changed_at)
);

CREATE TABLE login_attempt (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NULL,
  username VARCHAR(64) NOT NULL,
  source_ip VARBINARY(64) NULL,
  user_agent VARCHAR(255) NULL,
  success TINYINT(1) NOT NULL,
  reason_code VARCHAR(64) NULL,
  attempted_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_login_attempt_user FOREIGN KEY (user_id) REFERENCES account_user(id),
  KEY idx_login_attempt_username_time (username, attempted_at),
  KEY idx_login_attempt_user_time (user_id, attempted_at)
);

CREATE TABLE auth_session (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  session_token_hash CHAR(64) NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  last_activity_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  expires_at DATETIME(3) NOT NULL,
  revoked_at DATETIME(3) NULL,
  source_ip VARBINARY(64) NULL,
  user_agent VARCHAR(255) NULL,
  CONSTRAINT fk_auth_session_user FOREIGN KEY (user_id) REFERENCES account_user(id),
  UNIQUE KEY uk_auth_session_token_hash (session_token_hash),
  KEY idx_auth_session_user_status (user_id, status),
  KEY idx_auth_session_expires (expires_at),
  CONSTRAINT chk_auth_session_status CHECK (status IN ('ACTIVE','REVOKED','EXPIRED'))
);

CREATE TABLE security_policy (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  policy_key VARCHAR(64) NOT NULL,
  policy_value VARCHAR(256) NOT NULL,
  value_type VARCHAR(16) NOT NULL DEFAULT 'STRING',
  updated_by BIGINT UNSIGNED NULL,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_security_policy_key (policy_key),
  CONSTRAINT fk_security_policy_updated_by FOREIGN KEY (updated_by) REFERENCES account_user(id)
);

CREATE TABLE encryption_key_meta (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  key_version INT NOT NULL,
  key_alias VARCHAR(64) NOT NULL,
  status VARCHAR(16) NOT NULL,
  activated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  retired_at DATETIME(3) NULL,
  UNIQUE KEY uk_encryption_key_version (key_version),
  UNIQUE KEY uk_encryption_key_alias (key_alias),
  CONSTRAINT chk_encryption_key_status CHECK (status IN ('ACTIVE','RETIRED'))
);

CREATE TABLE approval_request (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  request_no VARCHAR(64) NOT NULL,
  request_type VARCHAR(64) NOT NULL,
  target_type VARCHAR(64) NOT NULL,
  target_id VARCHAR(64) NOT NULL,
  initiator_user_id BIGINT UNSIGNED NOT NULL,
  reason VARCHAR(1024) NOT NULL,
  payload_json JSON NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  required_approvals INT NOT NULL DEFAULT 1,
  approved_count INT NOT NULL DEFAULT 0,
  rejected_count INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  decided_at DATETIME(3) NULL,
  UNIQUE KEY uk_approval_request_no (request_no),
  KEY idx_approval_request_status_time (status, created_at),
  CONSTRAINT fk_approval_request_initiator FOREIGN KEY (initiator_user_id) REFERENCES account_user(id),
  CONSTRAINT chk_approval_request_status CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED'))
);

CREATE TABLE approval_action (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  request_id BIGINT UNSIGNED NOT NULL,
  approver_user_id BIGINT UNSIGNED NOT NULL,
  action VARCHAR(16) NOT NULL,
  comment_text VARCHAR(512) NULL,
  acted_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_approval_action_request FOREIGN KEY (request_id) REFERENCES approval_request(id),
  CONSTRAINT fk_approval_action_approver FOREIGN KEY (approver_user_id) REFERENCES account_user(id),
  KEY idx_approval_action_request_time (request_id, acted_at),
  UNIQUE KEY uk_approval_action_unique_actor (request_id, approver_user_id),
  CONSTRAINT chk_approval_action_action CHECK (action IN ('APPROVE','REJECT','CANCEL'))
);

CREATE TABLE brand (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  brand_code VARCHAR(64) NOT NULL,
  brand_name VARCHAR(128) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_brand_code (brand_code),
  UNIQUE KEY uk_brand_name (brand_name),
  KEY idx_brand_status (status),
  CONSTRAINT fk_brand_created_by FOREIGN KEY (created_by) REFERENCES account_user(id),
  CONSTRAINT chk_brand_status CHECK (status IN ('ACTIVE','DISABLED'))
);

CREATE TABLE category (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  category_code VARCHAR(64) NOT NULL,
  category_name VARCHAR(128) NOT NULL,
  parent_id BIGINT UNSIGNED NULL,
  depth TINYINT UNSIGNED NOT NULL,
  path VARCHAR(512) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_category_code (category_code),
  UNIQUE KEY uk_category_parent_name (parent_id, category_name),
  KEY idx_category_path (path),
  CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES category(id),
  CONSTRAINT fk_category_created_by FOREIGN KEY (created_by) REFERENCES account_user(id),
  CONSTRAINT chk_category_depth CHECK (depth BETWEEN 1 AND 4),
  CONSTRAINT chk_category_status CHECK (status IN ('ACTIVE','DISABLED'))
);

CREATE TABLE attribute_definition (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  attr_code VARCHAR(64) NOT NULL,
  attr_name VARCHAR(128) NOT NULL,
  value_type VARCHAR(16) NOT NULL,
  required_flag TINYINT(1) NOT NULL DEFAULT 0,
  scope_level VARCHAR(16) NOT NULL DEFAULT 'SKU',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_attr_code (attr_code),
  KEY idx_attr_scope_status (scope_level, status),
  CONSTRAINT fk_attribute_definition_created_by FOREIGN KEY (created_by) REFERENCES account_user(id),
  CONSTRAINT chk_attribute_value_type CHECK (value_type IN ('TEXT','NUMBER','BOOLEAN','ENUM','DATE')),
  CONSTRAINT chk_attribute_scope CHECK (scope_level IN ('PRODUCT','SKU')),
  CONSTRAINT chk_attribute_status CHECK (status IN ('ACTIVE','DISABLED'))
);

CREATE TABLE attribute_option (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  attr_id BIGINT UNSIGNED NOT NULL,
  option_value VARCHAR(128) NOT NULL,
  option_label VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_attribute_option_attr FOREIGN KEY (attr_id) REFERENCES attribute_definition(id),
  UNIQUE KEY uk_attribute_option_value (attr_id, option_value),
  KEY idx_attribute_option_status (attr_id, status),
  CONSTRAINT chk_attribute_option_status CHECK (status IN ('ACTIVE','DISABLED'))
);

CREATE TABLE product (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  product_code VARCHAR(64) NOT NULL,
  product_name VARCHAR(256) NOT NULL,
  merchant_id BIGINT UNSIGNED NOT NULL,
  brand_id BIGINT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  list_status VARCHAR(16) NOT NULL DEFAULT 'DELISTED',
  description_text TEXT NULL,
  main_image_path VARCHAR(255) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_product_code (product_code),
  KEY idx_product_merchant_status (merchant_id, status),
  KEY idx_product_category (category_id),
  CONSTRAINT fk_product_merchant FOREIGN KEY (merchant_id) REFERENCES account_user(id),
  CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand(id),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
  CONSTRAINT chk_product_status CHECK (status IN ('DRAFT','ACTIVE','DISABLED')),
  CONSTRAINT chk_product_list_status CHECK (list_status IN ('LISTED','DELISTED'))
);

CREATE TABLE product_attribute_value (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT UNSIGNED NOT NULL,
  attr_id BIGINT UNSIGNED NOT NULL,
  attr_value_text VARCHAR(512) NULL,
  attr_value_number DECIMAL(18,6) NULL,
  attr_value_bool TINYINT(1) NULL,
  attr_value_date DATE NULL,
  attr_option_id BIGINT UNSIGNED NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_product_attr_value_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_product_attr_value_attr FOREIGN KEY (attr_id) REFERENCES attribute_definition(id),
  CONSTRAINT fk_product_attr_value_option FOREIGN KEY (attr_option_id) REFERENCES attribute_option(id),
  UNIQUE KEY uk_product_attr_unique (product_id, attr_id)
);

CREATE TABLE sku (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT UNSIGNED NOT NULL,
  sku_code VARCHAR(64) NOT NULL,
  sku_barcode VARCHAR(64) NOT NULL,
  sku_name VARCHAR(256) NOT NULL,
  sale_price DECIMAL(12,2) NOT NULL,
  cost_price DECIMAL(12,2) NULL,
  weight_grams INT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES product(id),
  UNIQUE KEY uk_sku_code (sku_code),
  UNIQUE KEY uk_sku_barcode (sku_barcode),
  KEY idx_sku_product_status (product_id, status),
  CONSTRAINT chk_sku_status CHECK (status IN ('ACTIVE','DISABLED')),
  CONSTRAINT chk_sku_sale_price CHECK (sale_price >= 0)
);

CREATE TABLE sku_attribute_value (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  sku_id BIGINT UNSIGNED NOT NULL,
  attr_id BIGINT UNSIGNED NOT NULL,
  attr_value_text VARCHAR(512) NULL,
  attr_option_id BIGINT UNSIGNED NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_sku_attr_value_sku FOREIGN KEY (sku_id) REFERENCES sku(id),
  CONSTRAINT fk_sku_attr_value_attr FOREIGN KEY (attr_id) REFERENCES attribute_definition(id),
  CONSTRAINT fk_sku_attr_value_option FOREIGN KEY (attr_option_id) REFERENCES attribute_option(id),
  UNIQUE KEY uk_sku_attr_unique (sku_id, attr_id)
);

CREATE TABLE inventory (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  sku_id BIGINT UNSIGNED NOT NULL,
  available_qty INT NOT NULL DEFAULT 0,
  reserved_qty INT NOT NULL DEFAULT 0,
  total_qty INT NOT NULL DEFAULT 0,
  alert_threshold INT NOT NULL DEFAULT 10,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_inventory_sku FOREIGN KEY (sku_id) REFERENCES sku(id),
  UNIQUE KEY uk_inventory_sku (sku_id),
  KEY idx_inventory_alert_threshold (alert_threshold),
  CONSTRAINT chk_inventory_qty_nonnegative CHECK (available_qty >= 0 AND reserved_qty >= 0 AND total_qty >= 0),
  CONSTRAINT chk_inventory_alert_threshold CHECK (alert_threshold >= 0)
);

CREATE TABLE inventory_log (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  sku_id BIGINT UNSIGNED NOT NULL,
  change_type VARCHAR(32) NOT NULL,
  change_qty INT NOT NULL,
  before_qty INT NOT NULL,
  after_qty INT NOT NULL,
  reference_type VARCHAR(64) NULL,
  reference_id VARCHAR(64) NULL,
  operator_user_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_inventory_log_sku FOREIGN KEY (sku_id) REFERENCES sku(id),
  CONSTRAINT fk_inventory_log_operator FOREIGN KEY (operator_user_id) REFERENCES account_user(id),
  KEY idx_inventory_log_sku_time (sku_id, created_at),
  KEY idx_inventory_log_reference (reference_type, reference_id)
);

CREATE TABLE inventory_alert_event (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  sku_id BIGINT UNSIGNED NOT NULL,
  current_stock INT NOT NULL,
  threshold_value INT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  triggered_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  recovered_at DATETIME(3) NULL,
  handled_by BIGINT UNSIGNED NULL,
  handled_at DATETIME(3) NULL,
  CONSTRAINT fk_inventory_alert_event_sku FOREIGN KEY (sku_id) REFERENCES sku(id),
  CONSTRAINT fk_inventory_alert_event_handled_by FOREIGN KEY (handled_by) REFERENCES account_user(id),
  KEY idx_inventory_alert_event_status_time (status, triggered_at),
  CONSTRAINT chk_inventory_alert_event_status CHECK (status IN ('OPEN','RECOVERED','IGNORED'))
);

CREATE TABLE im_session (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  session_no VARCHAR(64) NOT NULL,
  session_type VARCHAR(16) NOT NULL DEFAULT 'P2P',
  creator_user_id BIGINT UNSIGNED NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_im_session_no (session_no),
  KEY idx_im_session_creator (creator_user_id),
  CONSTRAINT fk_im_session_creator FOREIGN KEY (creator_user_id) REFERENCES account_user(id),
  CONSTRAINT chk_im_session_type CHECK (session_type IN ('P2P','GROUP')),
  CONSTRAINT chk_im_session_status CHECK (status IN ('ACTIVE','CLOSED'))
);

CREATE TABLE im_session_member (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  role_in_session VARCHAR(16) NOT NULL DEFAULT 'MEMBER',
  joined_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  left_at DATETIME(3) NULL,
  mute_until DATETIME(3) NULL,
  CONSTRAINT fk_im_session_member_session FOREIGN KEY (session_id) REFERENCES im_session(id),
  CONSTRAINT fk_im_session_member_user FOREIGN KEY (user_id) REFERENCES account_user(id),
  UNIQUE KEY uk_im_session_member (session_id, user_id),
  KEY idx_im_session_member_user (user_id)
);

CREATE TABLE message (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  message_no VARCHAR(64) NOT NULL,
  session_id BIGINT UNSIGNED NOT NULL,
  sender_user_id BIGINT UNSIGNED NOT NULL,
  message_type VARCHAR(16) NOT NULL,
  content_text TEXT NULL,
  content_hash CHAR(64) NULL,
  image_path VARCHAR(255) NULL,
  image_mime VARCHAR(32) NULL,
  image_size_bytes INT NULL,
  image_fingerprint CHAR(64) NULL,
  folded_count INT NOT NULL DEFAULT 1,
  recalled_flag TINYINT(1) NOT NULL DEFAULT 0,
  recalled_at DATETIME(3) NULL,
  read_count INT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  expires_at DATETIME(3) NOT NULL,
  CONSTRAINT fk_message_session FOREIGN KEY (session_id) REFERENCES im_session(id),
  CONSTRAINT fk_message_sender FOREIGN KEY (sender_user_id) REFERENCES account_user(id),
  UNIQUE KEY uk_message_no (message_no),
  KEY idx_message_session_time (session_id, created_at),
  KEY idx_message_retention (expires_at),
  KEY idx_message_image_fingerprint (image_fingerprint),
  CONSTRAINT chk_message_type CHECK (message_type IN ('TEXT','IMAGE','SYSTEM')),
  CONSTRAINT chk_message_image_size CHECK (image_size_bytes IS NULL OR image_size_bytes <= 2097152),
  CONSTRAINT chk_message_image_mime CHECK (image_mime IS NULL OR image_mime IN ('image/jpeg','image/png'))
);

CREATE TABLE message_read_cursor (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  session_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  last_read_message_id BIGINT UNSIGNED NULL,
  unread_count INT NOT NULL DEFAULT 0,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_message_read_cursor_session FOREIGN KEY (session_id) REFERENCES im_session(id),
  CONSTRAINT fk_message_read_cursor_user FOREIGN KEY (user_id) REFERENCES account_user(id),
  CONSTRAINT fk_message_read_cursor_last_message FOREIGN KEY (last_read_message_id) REFERENCES message(id),
  UNIQUE KEY uk_message_read_cursor (session_id, user_id),
  KEY idx_message_read_cursor_user (user_id)
);

CREATE TABLE notification (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  notification_no VARCHAR(64) NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  channel VARCHAR(16) NOT NULL DEFAULT 'INTERNAL',
  category VARCHAR(32) NOT NULL,
  title VARCHAR(255) NOT NULL,
  content_text TEXT NOT NULL,
  payload_json JSON NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'UNREAD',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  read_at DATETIME(3) NULL,
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES account_user(id),
  UNIQUE KEY uk_notification_no (notification_no),
  KEY idx_notification_user_status_time (user_id, status, created_at),
  CONSTRAINT chk_notification_channel CHECK (channel IN ('INTERNAL')),
  CONSTRAINT chk_notification_status CHECK (status IN ('UNREAD','READ','ARCHIVED'))
);

CREATE TABLE notification_subscription (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  category VARCHAR(32) NOT NULL,
  enabled_flag TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_notification_subscription_user FOREIGN KEY (user_id) REFERENCES account_user(id),
  UNIQUE KEY uk_notification_subscription (user_id, category)
);

CREATE TABLE indicator_definition (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  indicator_code VARCHAR(64) NOT NULL,
  indicator_name VARCHAR(128) NOT NULL,
  domain VARCHAR(64) NOT NULL,
  metric_type VARCHAR(16) NOT NULL,
  unit VARCHAR(32) NULL,
  expression_sql TEXT NOT NULL,
  dimensions_json JSON NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  version INT NOT NULL DEFAULT 1,
  created_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_indicator_code (indicator_code),
  KEY idx_indicator_domain_status (domain, status),
  CONSTRAINT fk_indicator_definition_created_by FOREIGN KEY (created_by) REFERENCES account_user(id),
  CONSTRAINT chk_indicator_metric_type CHECK (metric_type IN ('COUNT','SUM','AVG','RATIO','CUSTOM')),
  CONSTRAINT chk_indicator_status CHECK (status IN ('ACTIVE','DISABLED'))
);

CREATE TABLE report_metrics (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  report_date DATE NOT NULL,
  report_scope VARCHAR(64) NOT NULL,
  scope_id VARCHAR(64) NULL,
  indicator_id BIGINT UNSIGNED NOT NULL,
  dimension_key VARCHAR(128) NULL,
  metric_value DECIMAL(20,6) NOT NULL,
  generated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_report_metrics_indicator FOREIGN KEY (indicator_id) REFERENCES indicator_definition(id),
  UNIQUE KEY uk_report_metrics_unique (report_date, report_scope, scope_id, indicator_id, dimension_key),
  KEY idx_report_metrics_indicator_date (indicator_id, report_date),
  KEY idx_report_metrics_scope_date (report_scope, report_date)
);

CREATE TABLE report_schedule (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  schedule_code VARCHAR(64) NOT NULL,
  schedule_name VARCHAR(128) NOT NULL,
  cron_expr VARCHAR(64) NOT NULL DEFAULT '0 0 2 * * ?',
  timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Shanghai',
  enabled_flag TINYINT(1) NOT NULL DEFAULT 1,
  retention_days INT NOT NULL DEFAULT 30,
  next_run_at DATETIME(3) NULL,
  created_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_report_schedule_code (schedule_code),
  KEY idx_report_schedule_enabled_next (enabled_flag, next_run_at),
  CONSTRAINT fk_report_schedule_created_by FOREIGN KEY (created_by) REFERENCES account_user(id),
  CONSTRAINT chk_report_schedule_retention CHECK (retention_days > 0)
);

CREATE TABLE job_execution (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  job_type VARCHAR(32) NOT NULL,
  schedule_id BIGINT UNSIGNED NULL,
  trigger_type VARCHAR(16) NOT NULL DEFAULT 'SCHEDULED',
  status VARCHAR(16) NOT NULL,
  started_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  ended_at DATETIME(3) NULL,
  duration_ms BIGINT NULL,
  error_code VARCHAR(64) NULL,
  error_message VARCHAR(1024) NULL,
  result_json JSON NULL,
  CONSTRAINT fk_job_execution_schedule FOREIGN KEY (schedule_id) REFERENCES report_schedule(id),
  KEY idx_job_execution_type_time (job_type, started_at),
  KEY idx_job_execution_status_time (status, started_at),
  CONSTRAINT chk_job_execution_type CHECK (job_type IN ('REPORT','RETENTION','BACKUP','CLEANUP')),
  CONSTRAINT chk_job_execution_status CHECK (status IN ('RUNNING','SUCCESS','FAILED','CANCELLED')),
  CONSTRAINT chk_job_execution_trigger CHECK (trigger_type IN ('SCHEDULED','MANUAL'))
);

CREATE TABLE backup_record (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  backup_no VARCHAR(64) NOT NULL,
  backup_type VARCHAR(16) NOT NULL,
  storage_path VARCHAR(255) NOT NULL,
  file_size_bytes BIGINT NULL,
  checksum_sha256 CHAR(64) NULL,
  started_at DATETIME(3) NOT NULL,
  completed_at DATETIME(3) NULL,
  status VARCHAR(16) NOT NULL,
  retention_until DATETIME(3) NOT NULL,
  trigger_source VARCHAR(16) NOT NULL DEFAULT 'SCHEDULED',
  initiated_by BIGINT UNSIGNED NULL,
  notes VARCHAR(512) NULL,
  UNIQUE KEY uk_backup_no (backup_no),
  KEY idx_backup_type_started (backup_type, started_at),
  KEY idx_backup_retention (retention_until),
  KEY idx_backup_status (status),
  CONSTRAINT fk_backup_record_initiated_by FOREIGN KEY (initiated_by) REFERENCES account_user(id),
  CONSTRAINT chk_backup_type CHECK (backup_type IN ('FULL','INCREMENTAL')),
  CONSTRAINT chk_backup_status CHECK (status IN ('RUNNING','SUCCESS','FAILED','EXPIRED')),
  CONSTRAINT chk_backup_trigger_source CHECK (trigger_source IN ('SCHEDULED','MANUAL'))
);

CREATE TABLE cooking_process (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  process_code VARCHAR(64) NOT NULL,
  process_name VARCHAR(128) NOT NULL,
  description_text TEXT NULL,
  owner_user_id BIGINT UNSIGNED NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_cooking_process_code (process_code),
  KEY idx_cooking_process_owner (owner_user_id),
  CONSTRAINT fk_cooking_process_owner FOREIGN KEY (owner_user_id) REFERENCES account_user(id),
  CONSTRAINT chk_cooking_process_status CHECK (status IN ('ACTIVE','DISABLED'))
);

CREATE TABLE cooking_step (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  process_id BIGINT UNSIGNED NOT NULL,
  step_no INT NOT NULL,
  step_name VARCHAR(128) NOT NULL,
  instruction_text TEXT NOT NULL,
  expected_seconds INT NULL,
  requires_timer TINYINT(1) NOT NULL DEFAULT 0,
  parallel_group_no INT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_cooking_step_process FOREIGN KEY (process_id) REFERENCES cooking_process(id),
  UNIQUE KEY uk_cooking_step_unique (process_id, step_no),
  KEY idx_cooking_step_parallel_group (process_id, parallel_group_no)
);

CREATE TABLE cooking_session_progress (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  process_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  current_step_no INT NOT NULL DEFAULT 1,
  status VARCHAR(16) NOT NULL DEFAULT 'RUNNING',
  progress_json JSON NULL,
  started_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  last_checkpoint_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  completed_at DATETIME(3) NULL,
  CONSTRAINT fk_cooking_session_progress_process FOREIGN KEY (process_id) REFERENCES cooking_process(id),
  CONSTRAINT fk_cooking_session_progress_user FOREIGN KEY (user_id) REFERENCES account_user(id),
  UNIQUE KEY uk_cooking_session_active (process_id, user_id, status),
  KEY idx_cooking_session_user_status (user_id, status),
  CONSTRAINT chk_cooking_session_status CHECK (status IN ('RUNNING','PAUSED','COMPLETED','ABORTED'))
);

CREATE TABLE cooking_timer (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  session_progress_id BIGINT UNSIGNED NOT NULL,
  step_id BIGINT UNSIGNED NOT NULL,
  timer_name VARCHAR(128) NOT NULL,
  duration_seconds INT NOT NULL,
  remaining_seconds INT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'RUNNING',
  reminder_interval_seconds INT NULL,
  started_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  ended_at DATETIME(3) NULL,
  CONSTRAINT fk_cooking_timer_session_progress FOREIGN KEY (session_progress_id) REFERENCES cooking_session_progress(id),
  CONSTRAINT fk_cooking_timer_step FOREIGN KEY (step_id) REFERENCES cooking_step(id),
  KEY idx_cooking_timer_status (session_progress_id, status),
  CONSTRAINT chk_cooking_timer_status CHECK (status IN ('RUNNING','PAUSED','COMPLETED','CANCELLED')),
  CONSTRAINT chk_cooking_timer_duration CHECK (duration_seconds > 0),
  CONSTRAINT chk_cooking_timer_remaining CHECK (remaining_seconds >= 0)
);

CREATE TABLE achievement_archive (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  achievement_no VARCHAR(64) NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  achievement_type VARCHAR(64) NOT NULL,
  title VARCHAR(255) NOT NULL,
  score DECIMAL(10,2) NULL,
  level_code VARCHAR(32) NULL,
  version INT NOT NULL,
  payload_json JSON NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_achievement_archive_no_version (achievement_no, version),
  KEY idx_achievement_archive_user (user_id),
  KEY idx_achievement_archive_type (achievement_type),
  CONSTRAINT fk_achievement_archive_user FOREIGN KEY (user_id) REFERENCES account_user(id),
  CONSTRAINT fk_achievement_archive_created_by FOREIGN KEY (created_by) REFERENCES account_user(id),
  CONSTRAINT chk_achievement_archive_version CHECK (version > 0),
  CONSTRAINT chk_achievement_archive_status CHECK (status IN ('ACTIVE','SUPERSEDED','REVOKED'))
);

CREATE TABLE attachment_version (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(64) NOT NULL,
  biz_id VARCHAR(64) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  mime_type VARCHAR(64) NOT NULL,
  size_bytes BIGINT NOT NULL,
  fingerprint_sha256 CHAR(64) NOT NULL,
  version INT NOT NULL,
  uploaded_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uk_attachment_version (biz_type, biz_id, version),
  KEY idx_attachment_biz (biz_type, biz_id),
  KEY idx_attachment_fingerprint (fingerprint_sha256),
  CONSTRAINT fk_attachment_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES account_user(id),
  CONSTRAINT chk_attachment_version_positive CHECK (version > 0)
);

CREATE TABLE audit_log (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  trace_id VARCHAR(64) NOT NULL,
  actor_user_id BIGINT UNSIGNED NULL,
  actor_role_codes VARCHAR(256) NULL,
  action_code VARCHAR(64) NOT NULL,
  target_type VARCHAR(64) NOT NULL,
  target_id VARCHAR(64) NOT NULL,
  request_id VARCHAR(64) NULL,
  source_ip VARBINARY(64) NULL,
  user_agent VARCHAR(255) NULL,
  before_json JSON NULL,
  after_json JSON NULL,
  result_code VARCHAR(32) NOT NULL,
  approval_request_id BIGINT UNSIGNED NULL,
  happened_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  hash_prev CHAR(64) NULL,
  hash_self CHAR(64) NOT NULL,
  signature VARBINARY(1024) NULL,
  CONSTRAINT fk_audit_log_actor FOREIGN KEY (actor_user_id) REFERENCES account_user(id),
  CONSTRAINT fk_audit_log_approval_request FOREIGN KEY (approval_request_id) REFERENCES approval_request(id),
  KEY idx_audit_log_target_time (target_type, target_id, happened_at),
  KEY idx_audit_log_actor_time (actor_user_id, happened_at),
  KEY idx_audit_log_action_time (action_code, happened_at),
  KEY idx_audit_log_trace (trace_id)
);

DELIMITER $$

CREATE TRIGGER trg_audit_log_no_update
BEFORE UPDATE ON audit_log
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'audit_log is immutable';
END$$

CREATE TRIGGER trg_audit_log_no_delete
BEFORE DELETE ON audit_log
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'audit_log is immutable';
END$$

CREATE TRIGGER trg_approval_action_not_initiator
BEFORE INSERT ON approval_action
FOR EACH ROW
BEGIN
  DECLARE v_initiator BIGINT UNSIGNED;
  SELECT initiator_user_id INTO v_initiator FROM approval_request WHERE id = NEW.request_id;
  IF v_initiator = NEW.approver_user_id THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'initiator cannot self-approve';
  END IF;
END$$

DELIMITER ;

INSERT INTO role (code, name) VALUES
('ADMIN', 'Administrator'),
('MERCHANT', 'Merchant'),
('BUYER', 'Buyer'),
('REVIEWER', 'Reviewer');

INSERT INTO security_policy (policy_key, policy_value, value_type) VALUES
('AUTH_PASSWORD_MIN_LENGTH', '8', 'NUMBER'),
('AUTH_PASSWORD_REQUIRE_ALNUM', 'true', 'BOOLEAN'),
('AUTH_LOGIN_MAX_FAILED', '5', 'NUMBER'),
('AUTH_LOCK_MINUTES', '15', 'NUMBER'),
('INVENTORY_DEFAULT_ALERT_THRESHOLD', '10', 'NUMBER'),
('IM_DUPLICATE_FOLD_SECONDS', '10', 'NUMBER'),
('IM_MESSAGE_RETENTION_DAYS', '180', 'NUMBER'),
('REPORT_DAILY_CRON', '0 0 2 * * ?', 'STRING'),
('BACKUP_FULL_CRON', '0 0 1 * * ?', 'STRING'),
('BACKUP_INCREMENTAL_CRON', '0 0 * * * ?', 'STRING'),
('BACKUP_RETENTION_DAYS', '30', 'NUMBER'),
('COOKING_PROGRESS_AUTOSAVE_SECONDS', '30', 'NUMBER');
