package com.company.petplatform.approval;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_action")
public class ApprovalActionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "request_id", nullable = false)
  private Long requestId;

  @Column(name = "approver_user_id", nullable = false)
  private Long approverUserId;

  @Column(nullable = false)
  private String action;

  @Column(name = "comment_text")
  private String commentText;

  @Column(name = "acted_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime actedAt;

  public Long getId() { return id; }
  public Long getRequestId() { return requestId; }
  public void setRequestId(Long requestId) { this.requestId = requestId; }
  public Long getApproverUserId() { return approverUserId; }
  public void setApproverUserId(Long approverUserId) { this.approverUserId = approverUserId; }
  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }
  public String getCommentText() { return commentText; }
  public void setCommentText(String commentText) { this.commentText = commentText; }
  public LocalDateTime getActedAt() { return actedAt; }
}
