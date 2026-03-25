package com.company.petplatform.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_user")
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "display_name", nullable = false, length = 128)
  private String displayName;

  @Column(name = "failed_login_count", nullable = false)
  private Integer failedLoginCount;

  @Column(name = "lock_until")
  private LocalDateTime lockUntil;

  @Column(nullable = false, length = 16)
  private String status;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  public Long getId() { return id; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }
  public Integer getFailedLoginCount() { return failedLoginCount; }
  public void setFailedLoginCount(Integer failedLoginCount) { this.failedLoginCount = failedLoginCount; }
  public LocalDateTime getLockUntil() { return lockUntil; }
  public void setLockUntil(LocalDateTime lockUntil) { this.lockUntil = lockUntil; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public LocalDateTime getLastLoginAt() { return lastLoginAt; }
  public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
