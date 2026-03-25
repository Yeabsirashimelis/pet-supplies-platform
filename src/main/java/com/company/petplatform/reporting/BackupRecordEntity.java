package com.company.petplatform.reporting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "backup_record")
public class BackupRecordEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "backup_no", nullable = false, unique = true)
  private String backupNo;

  @Column(name = "backup_type", nullable = false)
  private String backupType;

  @Column(name = "storage_path", nullable = false)
  private String storagePath;

  @Column(nullable = false)
  private String status;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "retention_until", nullable = false)
  private LocalDateTime retentionUntil;

  public Long getId() { return id; }
  public String getBackupNo() { return backupNo; }
  public void setBackupNo(String backupNo) { this.backupNo = backupNo; }
  public String getBackupType() { return backupType; }
  public void setBackupType(String backupType) { this.backupType = backupType; }
  public String getStoragePath() { return storagePath; }
  public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public LocalDateTime getStartedAt() { return startedAt; }
  public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
  public LocalDateTime getCompletedAt() { return completedAt; }
  public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
  public LocalDateTime getRetentionUntil() { return retentionUntil; }
  public void setRetentionUntil(LocalDateTime retentionUntil) { this.retentionUntil = retentionUntil; }
}
