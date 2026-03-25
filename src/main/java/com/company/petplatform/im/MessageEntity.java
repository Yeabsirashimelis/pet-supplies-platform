package com.company.petplatform.im;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class MessageEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "message_no", nullable = false, unique = true)
  private String messageNo;

  @Column(name = "session_id", nullable = false)
  private Long sessionId;

  @Column(name = "sender_user_id", nullable = false)
  private Long senderUserId;

  @Column(name = "message_type", nullable = false)
  private String messageType;

  @Column(name = "content_text", columnDefinition = "text")
  private String contentText;

  @Column(name = "content_hash", length = 64, columnDefinition = "char(64)")
  private String contentHash;

  @Column(name = "image_path")
  private String imagePath;

  @Column(name = "image_mime")
  private String imageMime;

  @Column(name = "image_size_bytes")
  private Integer imageSizeBytes;

  @Column(name = "image_fingerprint", length = 64, columnDefinition = "char(64)")
  private String imageFingerprint;

  @Column(name = "folded_count", nullable = false)
  private Integer foldedCount;

  @Column(name = "recalled_flag", nullable = false)
  private Boolean recalledFlag;

  @Column(name = "recalled_at")
  private LocalDateTime recalledAt;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  public Long getId() { return id; }
  public String getMessageNo() { return messageNo; }
  public void setMessageNo(String messageNo) { this.messageNo = messageNo; }
  public Long getSessionId() { return sessionId; }
  public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
  public Long getSenderUserId() { return senderUserId; }
  public void setSenderUserId(Long senderUserId) { this.senderUserId = senderUserId; }
  public String getMessageType() { return messageType; }
  public void setMessageType(String messageType) { this.messageType = messageType; }
  public String getContentText() { return contentText; }
  public void setContentText(String contentText) { this.contentText = contentText; }
  public String getContentHash() { return contentHash; }
  public void setContentHash(String contentHash) { this.contentHash = contentHash; }
  public String getImagePath() { return imagePath; }
  public void setImagePath(String imagePath) { this.imagePath = imagePath; }
  public String getImageMime() { return imageMime; }
  public void setImageMime(String imageMime) { this.imageMime = imageMime; }
  public Integer getImageSizeBytes() { return imageSizeBytes; }
  public void setImageSizeBytes(Integer imageSizeBytes) { this.imageSizeBytes = imageSizeBytes; }
  public String getImageFingerprint() { return imageFingerprint; }
  public void setImageFingerprint(String imageFingerprint) { this.imageFingerprint = imageFingerprint; }
  public Integer getFoldedCount() { return foldedCount; }
  public void setFoldedCount(Integer foldedCount) { this.foldedCount = foldedCount; }
  public Boolean getRecalledFlag() { return recalledFlag; }
  public void setRecalledFlag(Boolean recalledFlag) { this.recalledFlag = recalledFlag; }
  public LocalDateTime getRecalledAt() { return recalledAt; }
  public void setRecalledAt(LocalDateTime recalledAt) { this.recalledAt = recalledAt; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getExpiresAt() { return expiresAt; }
  public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
