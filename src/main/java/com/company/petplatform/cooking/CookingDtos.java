package com.company.petplatform.cooking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class CookingDtos {

  public record CreateProcessRequest(@NotBlank String processCode, @NotBlank String processName, String description) {
  }

  public record ProcessResponse(Long id, String processCode, String processName, String description, String status) {
  }

  public record AddStepRequest(
      @NotNull Integer stepNo,
      @NotBlank String stepName,
      @NotBlank String instruction,
      Integer expectedSeconds,
      @NotNull Boolean requiresTimer,
      Integer parallelGroupNo) {
  }

  public record StepResponse(Long id, Long processId, Integer stepNo, String stepName, String instruction, Integer expectedSeconds, Boolean requiresTimer, Integer parallelGroupNo) {
  }

  public record StartSessionRequest(@NotNull Long processId) {
  }

  public record SessionResponse(Long sessionProgressId, Long processId, Long userId, Integer currentStepNo, String status, LocalDateTime lastCheckpointAt) {
  }

  public record CheckpointRequest(@NotNull Integer currentStepNo, String progress) {
  }

  public record CreateTimerRequest(@NotNull Long stepId, @NotBlank String timerName, @NotNull Integer durationSeconds, Integer reminderIntervalSeconds) {
  }

  public record TimerResponse(Long timerId, Long sessionProgressId, Long stepId, String timerName, Integer durationSeconds, Integer remainingSeconds, String status) {
  }

  public record ResumeResponse(SessionResponse session, List<TimerResponse> activeTimers) {
  }
}
