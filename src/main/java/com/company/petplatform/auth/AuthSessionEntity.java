package com.company.petplatform.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_session")
public class AuthSessionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_token_hash", nullable = false, unique = true, length = 64, columnDefinition = "char(64)")
  private String sessionTokenHash;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false, length = 16)
  private String status;

  @Column(name = "last_activity_at", nullable = false)
  private LocalDateTime lastActivityAt;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "revoked_at")
  private LocalDateTime revokedAt;

  public Long getId() { return id; }
  public String getSessionTokenHash() { return sessionTokenHash; }
  public void setSessionTokenHash(String sessionTokenHash) { this.sessionTokenHash = sessionTokenHash; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public LocalDateTime getLastActivityAt() { return lastActivityAt; }
  public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }
  public LocalDateTime getExpiresAt() { return expiresAt; }
  public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
  public LocalDateTime getRevokedAt() { return revokedAt; }
  public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
}
