package com.company.petplatform.im;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "message_read_cursor")
public class MessageReadCursorEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "last_read_message_id")
  private Long lastReadMessageId;

  @Column(name = "unread_count", nullable = false)
  private Integer unreadCount;

  public Long getId() { return id; }
  public Long getSessionId() { return sessionId; }
  public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getLastReadMessageId() { return lastReadMessageId; }
  public void setLastReadMessageId(Long lastReadMessageId) { this.lastReadMessageId = lastReadMessageId; }
  public Integer getUnreadCount() { return unreadCount; }
  public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }
}
