package com.company.petplatform.cooking;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cooking")
public class CookingController {

  private final CookingService cookingService;

  public CookingController(CookingService cookingService) {
    this.cookingService = cookingService;
  }

  @PostMapping("/processes")
  public CookingDtos.ProcessResponse createProcess(@Valid @RequestBody CookingDtos.CreateProcessRequest request) {
    return cookingService.createProcess(request);
  }

  @PostMapping("/processes/{id}/steps")
  public CookingDtos.StepResponse addStep(@PathVariable Long id, @Valid @RequestBody CookingDtos.AddStepRequest request) {
    return cookingService.addStep(id, request);
  }

  @PostMapping("/sessions")
  public CookingDtos.SessionResponse start(@Valid @RequestBody CookingDtos.StartSessionRequest request) {
    return cookingService.startSession(request);
  }

  @PostMapping("/sessions/{id}/checkpoint")
  public CookingDtos.SessionResponse checkpoint(@PathVariable Long id, @Valid @RequestBody CookingDtos.CheckpointRequest request) {
    return cookingService.checkpoint(id, request);
  }

  @PostMapping("/sessions/{id}/timers")
  public CookingDtos.TimerResponse createTimer(@PathVariable Long id, @Valid @RequestBody CookingDtos.CreateTimerRequest request) {
    return cookingService.createTimer(id, request);
  }

  @PostMapping("/timers/{id}/pause")
  public CookingDtos.TimerResponse pause(@PathVariable Long id) {
    return cookingService.timerAction(id, "PAUSE");
  }

  @PostMapping("/timers/{id}/resume")
  public CookingDtos.TimerResponse resumeTimer(@PathVariable Long id) {
    return cookingService.timerAction(id, "RESUME");
  }

  @PostMapping("/timers/{id}/cancel")
  public CookingDtos.TimerResponse cancel(@PathVariable Long id) {
    return cookingService.timerAction(id, "CANCEL");
  }

  @PostMapping("/sessions/{id}/resume")
  public CookingDtos.ResumeResponse resume(@PathVariable Long id) {
    return cookingService.resume(id);
  }
}
