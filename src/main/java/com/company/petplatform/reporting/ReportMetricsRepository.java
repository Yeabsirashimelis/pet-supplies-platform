package com.company.petplatform.reporting;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportMetricsRepository extends JpaRepository<ReportMetricsEntity, Long> {
  List<ReportMetricsEntity> findByReportDateAndReportScopeAndScopeId(LocalDate reportDate, String reportScope, String scopeId);
  List<ReportMetricsEntity> findByIndicatorIdAndReportDate(Long indicatorId, LocalDate reportDate);
}
