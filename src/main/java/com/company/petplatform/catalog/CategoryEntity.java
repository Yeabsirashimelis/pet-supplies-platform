package com.company.petplatform.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "category")
public class CategoryEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "category_code", nullable = false, unique = true)
  private String categoryCode;

  @Column(name = "category_name", nullable = false)
  private String categoryName;

  @Column(name = "parent_id")
  private Long parentId;

  @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
  private Short depth;

  @Column(nullable = false)
  private String path;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  public Long getId() { return id; }
  public String getCategoryCode() { return categoryCode; }
  public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
  public String getCategoryName() { return categoryName; }
  public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
  public Long getParentId() { return parentId; }
  public void setParentId(Long parentId) { this.parentId = parentId; }
  public Short getDepth() { return depth; }
  public void setDepth(Short depth) { this.depth = depth; }
  public String getPath() { return path; }
  public void setPath(String path) { this.path = path; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Long getCreatedBy() { return createdBy; }
  public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
