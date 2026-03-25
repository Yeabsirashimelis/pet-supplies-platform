package com.company.petplatform.reporting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "report_metrics")
public class ReportMetricsEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "report_date", nullable = false)
  private LocalDate reportDate;

  @Column(name = "report_scope", nullable = false)
  private String reportScope;

  @Column(name = "scope_id")
  private String scopeId;

  @Column(name = "indicator_id", nullable = false)
  private Long indicatorId;

  @Column(name = "dimension_key")
  private String dimensionKey;

  @Column(name = "metric_value", nullable = false)
  private BigDecimal metricValue;

  public Long getId() { return id; }
  public LocalDate getReportDate() { return reportDate; }
  public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
  public String getReportScope() { return reportScope; }
  public void setReportScope(String reportScope) { this.reportScope = reportScope; }
  public String getScopeId() { return scopeId; }
  public void setScopeId(String scopeId) { this.scopeId = scopeId; }
  public Long getIndicatorId() { return indicatorId; }
  public void setIndicatorId(Long indicatorId) { this.indicatorId = indicatorId; }
  public String getDimensionKey() { return dimensionKey; }
  public void setDimensionKey(String dimensionKey) { this.dimensionKey = dimensionKey; }
  public BigDecimal getMetricValue() { return metricValue; }
  public void setMetricValue(BigDecimal metricValue) { this.metricValue = metricValue; }
}
