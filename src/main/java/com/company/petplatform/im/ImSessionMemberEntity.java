package com.company.petplatform.im;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "im_session_member")
public class ImSessionMemberEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "role_in_session", nullable = false)
  private String roleInSession;

  @Column(name = "left_at")
  private LocalDateTime leftAt;

  public Long getId() { return id; }
  public Long getSessionId() { return sessionId; }
  public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getRoleInSession() { return roleInSession; }
  public void setRoleInSession(String roleInSession) { this.roleInSession = roleInSession; }
  public LocalDateTime getLeftAt() { return leftAt; }
  public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }
}
