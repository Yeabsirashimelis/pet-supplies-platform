package com.company.petplatform.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory")
public class InventoryEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sku_id", nullable = false, unique = true)
  private Long skuId;

  @Column(name = "available_qty", nullable = false)
  private Integer availableQty;

  @Column(name = "reserved_qty", nullable = false)
  private Integer reservedQty;

  @Column(name = "total_qty", nullable = false)
  private Integer totalQty;

  @Column(name = "alert_threshold", nullable = false)
  private Integer alertThreshold;

  public Long getId() { return id; }
  public Long getSkuId() { return skuId; }
  public void setSkuId(Long skuId) { this.skuId = skuId; }
  public Integer getAvailableQty() { return availableQty; }
  public void setAvailableQty(Integer availableQty) { this.availableQty = availableQty; }
  public Integer getReservedQty() { return reservedQty; }
  public void setReservedQty(Integer reservedQty) { this.reservedQty = reservedQty; }
  public Integer getTotalQty() { return totalQty; }
  public void setTotalQty(Integer totalQty) { this.totalQty = totalQty; }
  public Integer getAlertThreshold() { return alertThreshold; }
  public void setAlertThreshold(Integer alertThreshold) { this.alertThreshold = alertThreshold; }
}
