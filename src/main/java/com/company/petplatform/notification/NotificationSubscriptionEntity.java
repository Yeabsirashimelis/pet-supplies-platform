package com.company.petplatform.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_subscription")
public class NotificationSubscriptionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String category;

  @Column(name = "enabled_flag", nullable = false)
  private Boolean enabledFlag;

  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public Boolean getEnabledFlag() { return enabledFlag; }
  public void setEnabledFlag(Boolean enabledFlag) { this.enabledFlag = enabledFlag; }
}
