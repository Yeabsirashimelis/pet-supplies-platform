package com.company.petplatform.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "sku")
public class SkuEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "sku_code", nullable = false, unique = true)
  private String skuCode;

  @Column(name = "sku_barcode", nullable = false, unique = true)
  private String skuBarcode;

  @Column(name = "sku_name", nullable = false)
  private String skuName;

  @Column(name = "sale_price", nullable = false)
  private BigDecimal salePrice;

  @Column(nullable = false)
  private String status;

  public Long getId() { return id; }
  public Long getProductId() { return productId; }
  public void setProductId(Long productId) { this.productId = productId; }
  public String getSkuCode() { return skuCode; }
  public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
  public String getSkuBarcode() { return skuBarcode; }
  public void setSkuBarcode(String skuBarcode) { this.skuBarcode = skuBarcode; }
  public String getSkuName() { return skuName; }
  public void setSkuName(String skuName) { this.skuName = skuName; }
  public BigDecimal getSalePrice() { return salePrice; }
  public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
