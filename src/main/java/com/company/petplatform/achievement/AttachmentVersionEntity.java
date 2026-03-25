package com.company.petplatform.achievement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "attachment_version")
public class AttachmentVersionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "biz_type", nullable = false)
  private String bizType;

  @Column(name = "biz_id", nullable = false)
  private String bizId;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "file_path", nullable = false)
  private String filePath;

  @Column(name = "mime_type", nullable = false)
  private String mimeType;

  @Column(name = "size_bytes", nullable = false)
  private Long sizeBytes;

  @Column(name = "fingerprint_sha256", nullable = false, length = 64, columnDefinition = "char(64)")
  private String fingerprintSha256;

  @Column(name = "version", nullable = false)
  private Integer version;

  @Column(name = "uploaded_by", nullable = false)
  private Long uploadedBy;

  public Long getId() { return id; }
  public String getBizType() { return bizType; }
  public void setBizType(String bizType) { this.bizType = bizType; }
  public String getBizId() { return bizId; }
  public void setBizId(String bizId) { this.bizId = bizId; }
  public String getFileName() { return fileName; }
  public void setFileName(String fileName) { this.fileName = fileName; }
  public String getFilePath() { return filePath; }
  public void setFilePath(String filePath) { this.filePath = filePath; }
  public String getMimeType() { return mimeType; }
  public void setMimeType(String mimeType) { this.mimeType = mimeType; }
  public Long getSizeBytes() { return sizeBytes; }
  public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
  public String getFingerprintSha256() { return fingerprintSha256; }
  public void setFingerprintSha256(String fingerprintSha256) { this.fingerprintSha256 = fingerprintSha256; }
  public Integer getVersion() { return version; }
  public void setVersion(Integer version) { this.version = version; }
  public Long getUploadedBy() { return uploadedBy; }
  public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }
}
