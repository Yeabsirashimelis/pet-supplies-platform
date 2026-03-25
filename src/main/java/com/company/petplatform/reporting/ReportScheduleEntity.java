package com.company.petplatform.reporting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_schedule")
public class ReportScheduleEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "schedule_code", nullable = false, unique = true)
  private String scheduleCode;

  @Column(name = "schedule_name", nullable = false)
  private String scheduleName;

  @Column(name = "cron_expr", nullable = false)
  private String cronExpr;

  @Column(name = "enabled_flag", nullable = false)
  private Boolean enabledFlag;

  @Column(name = "retention_days", nullable = false)
  private Integer retentionDays;

  @Column(name = "next_run_at")
  private LocalDateTime nextRunAt;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  public Long getId() { return id; }
  public String getScheduleCode() { return scheduleCode; }
  public void setScheduleCode(String scheduleCode) { this.scheduleCode = scheduleCode; }
  public String getScheduleName() { return scheduleName; }
  public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }
  public String getCronExpr() { return cronExpr; }
  public void setCronExpr(String cronExpr) { this.cronExpr = cronExpr; }
  public Boolean getEnabledFlag() { return enabledFlag; }
  public void setEnabledFlag(Boolean enabledFlag) { this.enabledFlag = enabledFlag; }
  public Integer getRetentionDays() { return retentionDays; }
  public void setRetentionDays(Integer retentionDays) { this.retentionDays = retentionDays; }
  public LocalDateTime getNextRunAt() { return nextRunAt; }
  public void setNextRunAt(LocalDateTime nextRunAt) { this.nextRunAt = nextRunAt; }
  public Long getCreatedBy() { return createdBy; }
  public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
