package com.company.petplatform.cooking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cooking_process")
public class CookingProcessEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "process_code", nullable = false, unique = true)
  private String processCode;

  @Column(name = "process_name", nullable = false)
  private String processName;

  @Column(name = "description_text", columnDefinition = "text")
  private String descriptionText;

  @Column(name = "owner_user_id", nullable = false)
  private Long ownerUserId;

  @Column(nullable = false)
  private String status;

  public Long getId() { return id; }
  public String getProcessCode() { return processCode; }
  public void setProcessCode(String processCode) { this.processCode = processCode; }
  public String getProcessName() { return processName; }
  public void setProcessName(String processName) { this.processName = processName; }
  public String getDescriptionText() { return descriptionText; }
  public void setDescriptionText(String descriptionText) { this.descriptionText = descriptionText; }
  public Long getOwnerUserId() { return ownerUserId; }
  public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
