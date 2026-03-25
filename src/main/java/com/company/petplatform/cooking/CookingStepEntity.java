package com.company.petplatform.cooking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cooking_step")
public class CookingStepEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "process_id", nullable = false)
  private Long processId;

  @Column(name = "step_no", nullable = false)
  private Integer stepNo;

  @Column(name = "step_name", nullable = false)
  private String stepName;

  @Column(name = "instruction_text", nullable = false, columnDefinition = "text")
  private String instructionText;

  @Column(name = "expected_seconds")
  private Integer expectedSeconds;

  @Column(name = "requires_timer", nullable = false)
  private Boolean requiresTimer;

  @Column(name = "parallel_group_no")
  private Integer parallelGroupNo;

  public Long getId() { return id; }
  public Long getProcessId() { return processId; }
  public void setProcessId(Long processId) { this.processId = processId; }
  public Integer getStepNo() { return stepNo; }
  public void setStepNo(Integer stepNo) { this.stepNo = stepNo; }
  public String getStepName() { return stepName; }
  public void setStepName(String stepName) { this.stepName = stepName; }
  public String getInstructionText() { return instructionText; }
  public void setInstructionText(String instructionText) { this.instructionText = instructionText; }
  public Integer getExpectedSeconds() { return expectedSeconds; }
  public void setExpectedSeconds(Integer expectedSeconds) { this.expectedSeconds = expectedSeconds; }
  public Boolean getRequiresTimer() { return requiresTimer; }
  public void setRequiresTimer(Boolean requiresTimer) { this.requiresTimer = requiresTimer; }
  public Integer getParallelGroupNo() { return parallelGroupNo; }
  public void setParallelGroupNo(Integer parallelGroupNo) { this.parallelGroupNo = parallelGroupNo; }
}
