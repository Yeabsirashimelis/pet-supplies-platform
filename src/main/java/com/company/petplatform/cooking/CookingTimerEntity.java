package com.company.petplatform.cooking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cooking_timer")
public class CookingTimerEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_progress_id", nullable = false)
  private Long sessionProgressId;

  @Column(name = "step_id", nullable = false)
  private Long stepId;

  @Column(name = "timer_name", nullable = false)
  private String timerName;

  @Column(name = "duration_seconds", nullable = false)
  private Integer durationSeconds;

  @Column(name = "remaining_seconds", nullable = false)
  private Integer remainingSeconds;

  @Column(nullable = false)
  private String status;

  @Column(name = "reminder_interval_seconds")
  private Integer reminderIntervalSeconds;

  public Long getId() { return id; }
  public Long getSessionProgressId() { return sessionProgressId; }
  public void setSessionProgressId(Long sessionProgressId) { this.sessionProgressId = sessionProgressId; }
  public Long getStepId() { return stepId; }
  public void setStepId(Long stepId) { this.stepId = stepId; }
  public String getTimerName() { return timerName; }
  public void setTimerName(String timerName) { this.timerName = timerName; }
  public Integer getDurationSeconds() { return durationSeconds; }
  public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
  public Integer getRemainingSeconds() { return remainingSeconds; }
  public void setRemainingSeconds(Integer remainingSeconds) { this.remainingSeconds = remainingSeconds; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Integer getReminderIntervalSeconds() { return reminderIntervalSeconds; }
  public void setReminderIntervalSeconds(Integer reminderIntervalSeconds) { this.reminderIntervalSeconds = reminderIntervalSeconds; }
}
