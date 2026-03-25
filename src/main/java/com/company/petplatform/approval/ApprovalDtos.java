package com.company.petplatform.approval;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class ApprovalDtos {

  public record CreateApprovalRequest(
      @NotBlank String requestType,
      @NotBlank String targetType,
      @NotBlank String targetId,
      @NotBlank String reason,
      @NotBlank String payload,
      @NotNull Integer requiredApprovals) {
  }

  public record ApprovalResponse(
      Long id,
      String requestNo,
      String requestType,
      String targetType,
      String targetId,
      Long initiatorUserId,
      String status,
      Integer requiredApprovals,
      Integer approvedCount,
      Integer rejectedCount,
      LocalDateTime createdAt,
      LocalDateTime decidedAt) {
  }

  public record ActionRequest(String comment) {
  }

  public record ApprovalPage(List<ApprovalResponse> items) {
  }
}
