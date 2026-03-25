package com.company.petplatform.reporting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_execution")
public class JobExecutionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "job_type", nullable = false)
  private String jobType;

  @Column(name = "schedule_id")
  private Long scheduleId;

  @Column(name = "trigger_type", nullable = false)
  private String triggerType;

  @Column(nullable = false)
  private String status;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "ended_at")
  private LocalDateTime endedAt;

  @Column(name = "error_code")
  private String errorCode;

  @Column(name = "error_message")
  private String errorMessage;

  public Long getId() { return id; }
  public String getJobType() { return jobType; }
  public void setJobType(String jobType) { this.jobType = jobType; }
  public Long getScheduleId() { return scheduleId; }
  public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
  public String getTriggerType() { return triggerType; }
  public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public LocalDateTime getStartedAt() { return startedAt; }
  public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
  public LocalDateTime getEndedAt() { return endedAt; }
  public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
  public String getErrorCode() { return errorCode; }
  public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
  public String getErrorMessage() { return errorMessage; }
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
