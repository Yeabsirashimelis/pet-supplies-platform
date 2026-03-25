package com.company.petplatform.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_alert_event")
public class InventoryAlertEventEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sku_id", nullable = false)
  private Long skuId;

  @Column(name = "current_stock", nullable = false)
  private Integer currentStock;

  @Column(name = "threshold_value", nullable = false)
  private Integer thresholdValue;

  @Column(nullable = false)
  private String status;

  @Column(name = "triggered_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime triggeredAt;

  @Column(name = "handled_by")
  private Long handledBy;

  @Column(name = "handled_at")
  private LocalDateTime handledAt;

  public Long getId() { return id; }
  public Long getSkuId() { return skuId; }
  public void setSkuId(Long skuId) { this.skuId = skuId; }
  public Integer getCurrentStock() { return currentStock; }
  public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }
  public Integer getThresholdValue() { return thresholdValue; }
  public void setThresholdValue(Integer thresholdValue) { this.thresholdValue = thresholdValue; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public LocalDateTime getTriggeredAt() { return triggeredAt; }
  public Long getHandledBy() { return handledBy; }
  public void setHandledBy(Long handledBy) { this.handledBy = handledBy; }
  public LocalDateTime getHandledAt() { return handledAt; }
  public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
}
