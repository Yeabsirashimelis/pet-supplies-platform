package com.company.petplatform.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class ProductEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_code", nullable = false, unique = true)
  private String productCode;

  @Column(name = "product_name", nullable = false)
  private String productName;

  @Column(name = "merchant_id", nullable = false)
  private Long merchantId;

  @Column(name = "brand_id", nullable = false)
  private Long brandId;

  @Column(name = "category_id", nullable = false)
  private Long categoryId;

  @Column(nullable = false)
  private String status;

  @Column(name = "list_status", nullable = false)
  private String listStatus;

  @Column(name = "description_text", columnDefinition = "text")
  private String descriptionText;

  public Long getId() { return id; }
  public String getProductCode() { return productCode; }
  public void setProductCode(String productCode) { this.productCode = productCode; }
  public String getProductName() { return productName; }
  public void setProductName(String productName) { this.productName = productName; }
  public Long getMerchantId() { return merchantId; }
  public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
  public Long getBrandId() { return brandId; }
  public void setBrandId(Long brandId) { this.brandId = brandId; }
  public Long getCategoryId() { return categoryId; }
  public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getListStatus() { return listStatus; }
  public void setListStatus(String listStatus) { this.listStatus = listStatus; }
  public String getDescriptionText() { return descriptionText; }
  public void setDescriptionText(String descriptionText) { this.descriptionText = descriptionText; }
}
