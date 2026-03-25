package com.company.petplatform.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class InventoryDtos {

  public record InventoryAdjustRequest(
      @NotNull Integer delta,
      @NotBlank String changeType,
      String referenceType,
      String referenceId) {
  }

  public record ThresholdRequest(@NotNull Integer alertThreshold) {
  }

  public record InventoryResponse(Long skuId, Integer availableQty, Integer reservedQty, Integer totalQty, Integer alertThreshold) {
  }

  public record AlertResponse(Long id, Long skuId, Integer currentStock, Integer thresholdValue, String status, LocalDateTime triggeredAt) {
  }

  public record AlertHandleRequest(@NotBlank String action) {
  }

  public record AlertsPage(List<AlertResponse> items) {
  }
}
