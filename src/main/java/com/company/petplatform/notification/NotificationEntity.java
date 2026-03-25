package com.company.petplatform.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class NotificationEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "notification_no", nullable = false, unique = true)
  private String notificationNo;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String channel;

  @Column(nullable = false)
  private String category;

  @Column(nullable = false)
  private String title;

  @Column(name = "content_text", nullable = false, columnDefinition = "text")
  private String contentText;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  public Long getId() { return id; }
  public String getNotificationNo() { return notificationNo; }
  public void setNotificationNo(String notificationNo) { this.notificationNo = notificationNo; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getChannel() { return channel; }
  public void setChannel(String channel) { this.channel = channel; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getContentText() { return contentText; }
  public void setContentText(String contentText) { this.contentText = contentText; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getReadAt() { return readAt; }
  public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
