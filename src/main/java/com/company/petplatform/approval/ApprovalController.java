package com.company.petplatform.approval;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/approvals/requests")
public class ApprovalController {

  private final ApprovalService approvalService;

  public ApprovalController(ApprovalService approvalService) {
    this.approvalService = approvalService;
  }

  @PostMapping
  public ApprovalDtos.ApprovalResponse create(@Valid @RequestBody ApprovalDtos.CreateApprovalRequest request) {
    return approvalService.create(request);
  }

  @GetMapping
  public ApprovalDtos.ApprovalPage list() {
    return approvalService.list();
  }

  @GetMapping("/{id}")
  public ApprovalDtos.ApprovalResponse get(@PathVariable Long id) {
    return approvalService.get(id);
  }

  @PostMapping("/{id}/approve")
  public ApprovalDtos.ApprovalResponse approve(@PathVariable Long id, @RequestBody(required = false) ApprovalDtos.ActionRequest request) {
    return approvalService.approve(id, request);
  }

  @PostMapping("/{id}/reject")
  public ApprovalDtos.ApprovalResponse reject(@PathVariable Long id, @RequestBody(required = false) ApprovalDtos.ActionRequest request) {
    return approvalService.reject(id, request);
  }

  @PostMapping("/{id}/cancel")
  public ApprovalDtos.ApprovalResponse cancel(@PathVariable Long id, @RequestBody(required = false) ApprovalDtos.ActionRequest request) {
    return approvalService.cancel(id, request);
  }
}
