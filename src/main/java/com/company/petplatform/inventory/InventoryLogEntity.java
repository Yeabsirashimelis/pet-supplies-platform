package com.company.petplatform.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_log")
public class InventoryLogEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sku_id", nullable = false)
  private Long skuId;

  @Column(name = "change_type", nullable = false)
  private String changeType;

  @Column(name = "change_qty", nullable = false)
  private Integer changeQty;

  @Column(name = "before_qty", nullable = false)
  private Integer beforeQty;

  @Column(name = "after_qty", nullable = false)
  private Integer afterQty;

  @Column(name = "reference_type")
  private String referenceType;

  @Column(name = "reference_id")
  private String referenceId;

  @Column(name = "operator_user_id", nullable = false)
  private Long operatorUserId;

  @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdAt;

  public Long getId() { return id; }
  public Long getSkuId() { return skuId; }
  public void setSkuId(Long skuId) { this.skuId = skuId; }
  public String getChangeType() { return changeType; }
  public void setChangeType(String changeType) { this.changeType = changeType; }
  public Integer getChangeQty() { return changeQty; }
  public void setChangeQty(Integer changeQty) { this.changeQty = changeQty; }
  public Integer getBeforeQty() { return beforeQty; }
  public void setBeforeQty(Integer beforeQty) { this.beforeQty = beforeQty; }
  public Integer getAfterQty() { return afterQty; }
  public void setAfterQty(Integer afterQty) { this.afterQty = afterQty; }
  public String getReferenceType() { return referenceType; }
  public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
  public String getReferenceId() { return referenceId; }
  public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
  public Long getOperatorUserId() { return operatorUserId; }
  public void setOperatorUserId(Long operatorUserId) { this.operatorUserId = operatorUserId; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
