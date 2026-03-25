package com.company.petplatform.approval;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_request")
public class ApprovalRequestEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "request_no", nullable = false, unique = true)
  private String requestNo;

  @Column(name = "request_type", nullable = false)
  private String requestType;

  @Column(name = "target_type", nullable = false)
  private String targetType;

  @Column(name = "target_id", nullable = false)
  private String targetId;

  @Column(name = "initiator_user_id", nullable = false)
  private Long initiatorUserId;

  @Column(nullable = false)
  private String reason;

  @Column(name = "payload_json", nullable = false, columnDefinition = "json")
  private String payloadJson;

  @Column(nullable = false)
  private String status;

  @Column(name = "required_approvals", nullable = false)
  private Integer requiredApprovals;

  @Column(name = "approved_count", nullable = false)
  private Integer approvedCount;

  @Column(name = "rejected_count", nullable = false)
  private Integer rejectedCount;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "decided_at")
  private LocalDateTime decidedAt;

  public Long getId() { return id; }
  public String getRequestNo() { return requestNo; }
  public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
  public String getRequestType() { return requestType; }
  public void setRequestType(String requestType) { this.requestType = requestType; }
  public String getTargetType() { return targetType; }
  public void setTargetType(String targetType) { this.targetType = targetType; }
  public String getTargetId() { return targetId; }
  public void setTargetId(String targetId) { this.targetId = targetId; }
  public Long getInitiatorUserId() { return initiatorUserId; }
  public void setInitiatorUserId(Long initiatorUserId) { this.initiatorUserId = initiatorUserId; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public String getPayloadJson() { return payloadJson; }
  public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Integer getRequiredApprovals() { return requiredApprovals; }
  public void setRequiredApprovals(Integer requiredApprovals) { this.requiredApprovals = requiredApprovals; }
  public Integer getApprovedCount() { return approvedCount; }
  public void setApprovedCount(Integer approvedCount) { this.approvedCount = approvedCount; }
  public Integer getRejectedCount() { return rejectedCount; }
  public void setRejectedCount(Integer rejectedCount) { this.rejectedCount = rejectedCount; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getDecidedAt() { return decidedAt; }
  public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
}
