package com.company.petplatform.reporting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportingDtos {

  public record CreateIndicatorRequest(
      @NotBlank String indicatorCode,
      @NotBlank String indicatorName,
      @NotBlank String domain,
      @NotBlank String metricType,
      @NotBlank String expressionSql) {
  }

  public record IndicatorResponse(Long id, String indicatorCode, String indicatorName, String domain, String metricType, String status) {
  }

  public record AggregateRequest(@NotNull LocalDate date, @NotBlank String scope, String scopeId, @NotEmpty List<String> indicatorCodes) {
  }

  public record MetricResponse(String indicatorCode, LocalDate date, String scope, String scopeId, BigDecimal value) {
  }

  public record MetricsPage(List<MetricResponse> items) {
  }

  public record ScheduleRequest(
      @NotBlank String scheduleCode,
      @NotBlank String scheduleName,
      @NotBlank String cronExpr,
      @NotNull Boolean enabled,
      @NotNull Integer retentionDays) {
  }

  public record ScheduleResponse(Long id, String scheduleCode, String scheduleName, String cronExpr, Boolean enabled, Integer retentionDays) {
  }

  public record ExportResponse(String filePath) {
  }

  public record JobResponse(Long jobExecutionId, String status) {
  }
}
