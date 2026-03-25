package com.company.petplatform.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "attribute_definition")
public class AttributeDefinitionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "attr_code", nullable = false, unique = true)
  private String attrCode;

  @Column(name = "attr_name", nullable = false)
  private String attrName;

  @Column(name = "value_type", nullable = false)
  private String valueType;

  @Column(name = "scope_level", nullable = false)
  private String scopeLevel;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_by", nullable = false)
  private Long createdBy;

  public Long getId() { return id; }
  public String getAttrCode() { return attrCode; }
  public void setAttrCode(String attrCode) { this.attrCode = attrCode; }
  public String getAttrName() { return attrName; }
  public void setAttrName(String attrName) { this.attrName = attrName; }
  public String getValueType() { return valueType; }
  public void setValueType(String valueType) { this.valueType = valueType; }
  public String getScopeLevel() { return scopeLevel; }
  public void setScopeLevel(String scopeLevel) { this.scopeLevel = scopeLevel; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public Long getCreatedBy() { return createdBy; }
  public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
}
