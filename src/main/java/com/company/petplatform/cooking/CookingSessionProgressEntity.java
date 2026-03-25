package com.company.petplatform.cooking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "cooking_session_progress")
public class CookingSessionProgressEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "process_id", nullable = false)
  private Long processId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "current_step_no", nullable = false)
  private Integer currentStepNo;

  @Column(nullable = false)
  private String status;

  @Column(name = "progress_json", columnDefinition = "json")
  private String progressJson;

  @Column(name = "last_checkpoint_at", nullable = false)
  private LocalDateTime lastCheckpointAt;

  public Long getId() { return id; }
  public Long getProcessId() { return processId; }
  public void setProcessId(Long processId) { this.processId = processId; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Integer getCurrentStepNo() { return currentStepNo; }
  public void setCurrentStepNo(Integer currentStepNo) { this.currentStepNo = currentStepNo; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getProgressJson() { return progressJson; }
  public void setProgressJson(String progressJson) { this.progressJson = progressJson; }
  public LocalDateTime getLastCheckpointAt() { return lastCheckpointAt; }
  public void setLastCheckpointAt(LocalDateTime lastCheckpointAt) { this.lastCheckpointAt = lastCheckpointAt; }
}
