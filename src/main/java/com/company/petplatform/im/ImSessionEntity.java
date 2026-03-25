package com.company.petplatform.im;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "im_session")
public class ImSessionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_no", nullable = false, unique = true)
  private String sessionNo;

  @Column(name = "session_type", nullable = false)
  private String sessionType;

  @Column(name = "creator_user_id", nullable = false)
  private Long creatorUserId;

  @Column(nullable = false)
  private String status;

  public Long getId() { return id; }
  public String getSessionNo() { return sessionNo; }
  public void setSessionNo(String sessionNo) { this.sessionNo = sessionNo; }
  public String getSessionType() { return sessionType; }
  public void setSessionType(String sessionType) { this.sessionType = sessionType; }
  public Long getCreatorUserId() { return creatorUserId; }
  public void setCreatorUserId(Long creatorUserId) { this.creatorUserId = creatorUserId; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
