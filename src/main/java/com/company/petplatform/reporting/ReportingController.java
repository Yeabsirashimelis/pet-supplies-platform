package com.company.petplatform.reporting;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportingController {

  private final ReportingService reportingService;

  public ReportingController(ReportingService reportingService) {
    this.reportingService = reportingService;
  }

  @PostMapping("/indicators")
  public ReportingDtos.IndicatorResponse createIndicator(@Valid @RequestBody ReportingDtos.CreateIndicatorRequest request) {
    return reportingService.createIndicator(request);
  }

  @GetMapping("/indicators")
  public List<ReportingDtos.IndicatorResponse> listIndicators() {
    return reportingService.listIndicators();
  }

  @PostMapping("/aggregate")
  public ReportingDtos.MetricsPage aggregate(@Valid @RequestBody ReportingDtos.AggregateRequest request) {
    return reportingService.aggregate(request);
  }

  @PostMapping("/drilldown")
  public ReportingDtos.MetricsPage drilldown(
      @RequestParam String indicatorCode,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return reportingService.drilldown(indicatorCode, date);
  }

  @PostMapping("/export")
  public ReportingDtos.ExportResponse export() {
    return reportingService.export();
  }

  @PostMapping("/schedules")
  public ReportingDtos.ScheduleResponse createSchedule(@Valid @RequestBody ReportingDtos.ScheduleRequest request) {
    return reportingService.createSchedule(request);
  }

  @GetMapping("/schedules")
  public List<ReportingDtos.ScheduleResponse> listSchedules() {
    return reportingService.listSchedules();
  }

  @PutMapping("/schedules/{id}")
  public ReportingDtos.ScheduleResponse updateSchedule(@PathVariable Long id, @Valid @RequestBody ReportingDtos.ScheduleRequest request) {
    return reportingService.updateSchedule(id, request);
  }

  @PostMapping("/schedules/{id}/run")
  public ReportingDtos.JobResponse run(@PathVariable Long id) {
    return reportingService.runSchedule(id);
  }

  @GetMapping("/jobs/{id}")
  public ReportingDtos.JobResponse getJob(@PathVariable Long id) {
    return reportingService.getJob(id);
  }
}
