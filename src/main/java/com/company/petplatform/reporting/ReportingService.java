package com.company.petplatform.reporting;

import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportingService {

  private final IndicatorDefinitionRepository indicatorDefinitionRepository;
  private final ReportMetricsRepository reportMetricsRepository;
  private final ReportScheduleRepository reportScheduleRepository;
  private final JobExecutionRepository jobExecutionRepository;
  private final AuthContext authContext;

  public ReportingService(
      IndicatorDefinitionRepository indicatorDefinitionRepository,
      ReportMetricsRepository reportMetricsRepository,
      ReportScheduleRepository reportScheduleRepository,
      JobExecutionRepository jobExecutionRepository,
      AuthContext authContext) {
    this.indicatorDefinitionRepository = indicatorDefinitionRepository;
    this.reportMetricsRepository = reportMetricsRepository;
    this.reportScheduleRepository = reportScheduleRepository;
    this.jobExecutionRepository = jobExecutionRepository;
    this.authContext = authContext;
  }

  @Transactional
  public ReportingDtos.IndicatorResponse createIndicator(ReportingDtos.CreateIndicatorRequest req) {
    authContext.requireAnyRole("ADMIN", "REVIEWER");
    CurrentUser user = authContext.currentUser();
    IndicatorDefinitionEntity entity = new IndicatorDefinitionEntity();
    entity.setIndicatorCode(req.indicatorCode());
    entity.setIndicatorName(req.indicatorName());
    entity.setDomain(req.domain());
    entity.setMetricType(req.metricType());
    entity.setExpressionSql(req.expressionSql());
    entity.setStatus("ACTIVE");
    entity.setCreatedBy(user.id());
    IndicatorDefinitionEntity saved = indicatorDefinitionRepository.save(entity);
    return new ReportingDtos.IndicatorResponse(saved.getId(), saved.getIndicatorCode(), saved.getIndicatorName(), saved.getDomain(), saved.getMetricType(), saved.getStatus());
  }

  @Transactional(readOnly = true)
  public List<ReportingDtos.IndicatorResponse> listIndicators() {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    return indicatorDefinitionRepository.findAll().stream()
        .map(i -> new ReportingDtos.IndicatorResponse(i.getId(), i.getIndicatorCode(), i.getIndicatorName(), i.getDomain(), i.getMetricType(), i.getStatus()))
        .toList();
  }

  @Transactional
  public ReportingDtos.MetricsPage aggregate(ReportingDtos.AggregateRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    List<ReportingDtos.MetricResponse> result = req.indicatorCodes().stream().map(code -> {
      IndicatorDefinitionEntity indicator = indicatorDefinitionRepository.findByIndicatorCode(code)
          .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Indicator not found: " + code, HttpStatus.NOT_FOUND));
      List<ReportMetricsEntity> metrics = reportMetricsRepository.findByReportDateAndReportScopeAndScopeId(req.date(), req.scope(), req.scopeId());
      BigDecimal value = metrics.stream()
          .filter(m -> m.getIndicatorId().equals(indicator.getId()))
          .map(ReportMetricsEntity::getMetricValue)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      return new ReportingDtos.MetricResponse(code, req.date(), req.scope(), req.scopeId(), value);
    }).toList();
    return new ReportingDtos.MetricsPage(result);
  }

  @Transactional(readOnly = true)
  public ReportingDtos.MetricsPage drilldown(String indicatorCode, LocalDate date) {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    IndicatorDefinitionEntity indicator = indicatorDefinitionRepository.findByIndicatorCode(indicatorCode)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Indicator not found", HttpStatus.NOT_FOUND));
    List<ReportingDtos.MetricResponse> items = reportMetricsRepository.findByIndicatorIdAndReportDate(indicator.getId(), date).stream()
        .map(m -> new ReportingDtos.MetricResponse(indicatorCode, m.getReportDate(), m.getReportScope(), m.getScopeId(), m.getMetricValue()))
        .toList();
    return new ReportingDtos.MetricsPage(items);
  }

  @Transactional(readOnly = true)
  public ReportingDtos.ExportResponse export() {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    return new ReportingDtos.ExportResponse("/exports/reports/report_" + System.currentTimeMillis() + ".csv");
  }

  @Transactional
  public ReportingDtos.ScheduleResponse createSchedule(ReportingDtos.ScheduleRequest req) {
    authContext.requireAnyRole("ADMIN");
    CurrentUser user = authContext.currentUser();
    ReportScheduleEntity s = new ReportScheduleEntity();
    s.setScheduleCode(req.scheduleCode());
    s.setScheduleName(req.scheduleName());
    s.setCronExpr(req.cronExpr());
    s.setEnabledFlag(req.enabled());
    s.setRetentionDays(req.retentionDays());
    s.setCreatedBy(user.id());
    ReportScheduleEntity saved = reportScheduleRepository.save(s);
    return toSchedule(saved);
  }

  @Transactional(readOnly = true)
  public List<ReportingDtos.ScheduleResponse> listSchedules() {
    authContext.requireAnyRole("ADMIN", "REVIEWER");
    return reportScheduleRepository.findAll().stream().map(this::toSchedule).toList();
  }

  @Transactional
  public ReportingDtos.ScheduleResponse updateSchedule(Long id, ReportingDtos.ScheduleRequest req) {
    authContext.requireAnyRole("ADMIN");
    ReportScheduleEntity s = reportScheduleRepository.findById(id)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Schedule not found", HttpStatus.NOT_FOUND));
    s.setScheduleName(req.scheduleName());
    s.setCronExpr(req.cronExpr());
    s.setEnabledFlag(req.enabled());
    s.setRetentionDays(req.retentionDays());
    return toSchedule(reportScheduleRepository.save(s));
  }

  @Transactional
  public ReportingDtos.JobResponse runSchedule(Long id) {
    authContext.requireAnyRole("ADMIN");
    ReportScheduleEntity schedule = reportScheduleRepository.findById(id)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Schedule not found", HttpStatus.NOT_FOUND));
    JobExecutionEntity job = new JobExecutionEntity();
    job.setJobType("REPORT");
    job.setScheduleId(schedule.getId());
    job.setTriggerType("MANUAL");
    job.setStatus("RUNNING");
    job.setStartedAt(LocalDateTime.now());
    JobExecutionEntity saved = jobExecutionRepository.save(job);
    saved.setStatus("SUCCESS");
    saved.setEndedAt(LocalDateTime.now());
    jobExecutionRepository.save(saved);
    return new ReportingDtos.JobResponse(saved.getId(), saved.getStatus());
  }

  @Transactional(readOnly = true)
  public ReportingDtos.JobResponse getJob(Long id) {
    authContext.requireAnyRole("ADMIN", "REVIEWER");
    JobExecutionEntity job = jobExecutionRepository.findById(id)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Job not found", HttpStatus.NOT_FOUND));
    return new ReportingDtos.JobResponse(job.getId(), job.getStatus());
  }

  private ReportingDtos.ScheduleResponse toSchedule(ReportScheduleEntity s) {
    return new ReportingDtos.ScheduleResponse(s.getId(), s.getScheduleCode(), s.getScheduleName(), s.getCronExpr(), s.getEnabledFlag(), s.getRetentionDays());
  }
}
