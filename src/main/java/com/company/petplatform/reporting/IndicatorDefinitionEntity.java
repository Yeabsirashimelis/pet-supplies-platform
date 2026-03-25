package com.company.petplatform.reporting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "indicator_definition")
public class IndicatorDefinitionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "indicator_code", nullable = false, unique = true)
  private String indicatorCode;

  @Column(name = "indicator_name", nullable = false)
  private String indicatorName;

  @Column(nullable = false)
  private String domain;

  @Column(name = "metric_type", nullable = false)
  private String metricType;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  @Column(name = "expression_sql", nullable = false, columnDefinition = "text")
  private String expressionSql;

  public Long getId() { return id; }
  public String getIndicatorCode() { return indicatorCode; }
  public void setIndicatorCode(String indicatorCode) { this.indicatorCode = indicatorCode; }
  public String getIndicatorName() { return indicatorName; }
  public void setIndicatorName(String indicatorName) { this.indicatorName = indicatorName; }
  public String getDomain() { return domain; }
  public void setDomain(String domain) { this.domain = domain; }
  public String getMetricType() { return metricType; }
  public void setMetricType(String metricType) { this.metricType = metricType; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Long getCreatedBy() { return createdBy; }
  public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
  public String getExpressionSql() { return expressionSql; }
  public void setExpressionSql(String expressionSql) { this.expressionSql = expressionSql; }
}
