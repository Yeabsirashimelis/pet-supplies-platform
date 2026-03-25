package com.company.petplatform.achievement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievement_archive")
public class AchievementArchiveEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "achievement_no", nullable = false)
  private String achievementNo;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "achievement_type", nullable = false)
  private String achievementType;

  @Column(nullable = false)
  private String title;

  @Column
  private BigDecimal score;

  @Column(name = "version", nullable = false)
  private Integer version;

  @Column(name = "payload_json", nullable = false, columnDefinition = "json")
  private String payloadJson;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;

  public Long getId() { return id; }
  public String getAchievementNo() { return achievementNo; }
  public void setAchievementNo(String achievementNo) { this.achievementNo = achievementNo; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getAchievementType() { return achievementType; }
  public void setAchievementType(String achievementType) { this.achievementType = achievementType; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public BigDecimal getScore() { return score; }
  public void setScore(BigDecimal score) { this.score = score; }
  public Integer getVersion() { return version; }
  public void setVersion(Integer version) { this.version = version; }
  public String getPayloadJson() { return payloadJson; }
  public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Long getCreatedBy() { return createdBy; }
  public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
