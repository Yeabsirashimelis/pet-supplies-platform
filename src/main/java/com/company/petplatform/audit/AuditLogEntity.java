package com.company.petplatform.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLogEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "trace_id", nullable = false)
  private String traceId;

  @Column(name = "actor_user_id")
  private Long actorUserId;

  @Column(name = "action_code", nullable = false)
  private String actionCode;

  @Column(name = "target_type", nullable = false)
  private String targetType;

  @Column(name = "target_id", nullable = false)
  private String targetId;

  @Column(name = "result_code", nullable = false)
  private String resultCode;

  @Column(name = "request_id")
  private String requestId;

  @Column(name = "hash_prev", length = 64, columnDefinition = "char(64)")
  private String hashPrev;

  @Column(name = "hash_self", nullable = false, length = 64, columnDefinition = "char(64)")
  private String hashSelf;

  @Column(name = "happened_at", nullable = false)
  private LocalDateTime happenedAt;

  public Long getId() { return id; }
  public String getTraceId() { return traceId; }
  public void setTraceId(String traceId) { this.traceId = traceId; }
  public Long getActorUserId() { return actorUserId; }
  public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
  public String getActionCode() { return actionCode; }
  public void setActionCode(String actionCode) { this.actionCode = actionCode; }
  public String getTargetType() { return targetType; }
  public void setTargetType(String targetType) { this.targetType = targetType; }
  public String getTargetId() { return targetId; }
  public void setTargetId(String targetId) { this.targetId = targetId; }
  public String getResultCode() { return resultCode; }
  public void setResultCode(String resultCode) { this.resultCode = resultCode; }
  public String getRequestId() { return requestId; }
  public void setRequestId(String requestId) { this.requestId = requestId; }
  public String getHashPrev() { return hashPrev; }
  public void setHashPrev(String hashPrev) { this.hashPrev = hashPrev; }
  public String getHashSelf() { return hashSelf; }
  public void setHashSelf(String hashSelf) { this.hashSelf = hashSelf; }
  public LocalDateTime getHappenedAt() { return happenedAt; }
  public void setHappenedAt(LocalDateTime happenedAt) { this.happenedAt = happenedAt; }
}
