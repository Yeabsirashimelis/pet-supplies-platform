package com.company.petplatform.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "brand")
public class BrandEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "brand_code", nullable = false, unique = true)
  private String brandCode;

  @Column(name = "brand_name", nullable = false, unique = true)
  private String brandName;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  public Long getId() { return id; }
  public String getBrandCode() { return brandCode; }
  public void setBrandCode(String brandCode) { this.brandCode = brandCode; }
  public String getBrandName() { return brandName; }
  public void setBrandName(String brandName) { this.brandName = brandName; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Long getCreatedBy() { return createdBy; }
  public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
