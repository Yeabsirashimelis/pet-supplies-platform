package com.company.petplatform.inventory;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

  private final InventoryService inventoryService;

  public InventoryController(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @PutMapping("/skus/{skuId}")
  public InventoryDtos.InventoryResponse adjustInventory(
      @PathVariable Long skuId,
      @Valid @RequestBody InventoryDtos.InventoryAdjustRequest request) {
    return inventoryService.adjustInventory(skuId, request);
  }

  @PutMapping("/skus/{skuId}/threshold")
  public InventoryDtos.InventoryResponse updateThreshold(
      @PathVariable Long skuId,
      @Valid @RequestBody InventoryDtos.ThresholdRequest request) {
    return inventoryService.setThreshold(skuId, request);
  }

  @GetMapping("/alerts")
  public InventoryDtos.AlertsPage alerts(@RequestParam(required = false) String status) {
    return inventoryService.listAlerts(status);
  }

  @PostMapping("/alerts/{id}/handle")
  public InventoryDtos.AlertResponse handle(
      @PathVariable Long id,
      @Valid @RequestBody InventoryDtos.AlertHandleRequest request) {
    return inventoryService.handleAlert(id, request);
  }
}
