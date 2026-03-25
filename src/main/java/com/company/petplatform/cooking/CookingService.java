package com.company.petplatform.cooking;

import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CookingService {

  private final CookingProcessRepository cookingProcessRepository;
  private final CookingStepRepository cookingStepRepository;
  private final CookingSessionProgressRepository cookingSessionProgressRepository;
  private final CookingTimerRepository cookingTimerRepository;
  private final AuthContext authContext;

  public CookingService(
      CookingProcessRepository cookingProcessRepository,
      CookingStepRepository cookingStepRepository,
      CookingSessionProgressRepository cookingSessionProgressRepository,
      CookingTimerRepository cookingTimerRepository,
      AuthContext authContext) {
    this.cookingProcessRepository = cookingProcessRepository;
    this.cookingStepRepository = cookingStepRepository;
    this.cookingSessionProgressRepository = cookingSessionProgressRepository;
    this.cookingTimerRepository = cookingTimerRepository;
    this.authContext = authContext;
  }

  @Transactional
  public CookingDtos.ProcessResponse createProcess(CookingDtos.CreateProcessRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    CurrentUser user = authContext.currentUser();
    CookingProcessEntity p = new CookingProcessEntity();
    p.setProcessCode(req.processCode());
    p.setProcessName(req.processName());
    p.setDescriptionText(req.description());
    p.setOwnerUserId(user.id());
    p.setStatus("ACTIVE");
    CookingProcessEntity saved = cookingProcessRepository.save(p);
    return new CookingDtos.ProcessResponse(saved.getId(), saved.getProcessCode(), saved.getProcessName(), saved.getDescriptionText(), saved.getStatus());
  }

  @Transactional
  public CookingDtos.StepResponse addStep(Long processId, CookingDtos.AddStepRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    if (!cookingProcessRepository.existsById(processId)) {
      throw new ApiException("RESOURCE_NOT_FOUND", "Process not found", org.springframework.http.HttpStatus.NOT_FOUND);
    }
    CookingStepEntity s = new CookingStepEntity();
    s.setProcessId(processId);
    s.setStepNo(req.stepNo());
    s.setStepName(req.stepName());
    s.setInstructionText(req.instruction());
    s.setExpectedSeconds(req.expectedSeconds());
    s.setRequiresTimer(req.requiresTimer());
    s.setParallelGroupNo(req.parallelGroupNo());
    CookingStepEntity saved = cookingStepRepository.save(s);
    return new CookingDtos.StepResponse(saved.getId(), saved.getProcessId(), saved.getStepNo(), saved.getStepName(), saved.getInstructionText(), saved.getExpectedSeconds(), saved.getRequiresTimer(), saved.getParallelGroupNo());
  }

  @Transactional
  public CookingDtos.SessionResponse startSession(CookingDtos.StartSessionRequest req) {
    CurrentUser user = authContext.currentUser();
    if (!cookingProcessRepository.existsById(req.processId())) {
      throw new ApiException("RESOURCE_NOT_FOUND", "Process not found", org.springframework.http.HttpStatus.NOT_FOUND);
    }
    CookingSessionProgressEntity sp = new CookingSessionProgressEntity();
    sp.setProcessId(req.processId());
    sp.setUserId(user.id());
    sp.setCurrentStepNo(1);
    sp.setStatus("RUNNING");
    sp.setProgressJson(null);
    sp.setLastCheckpointAt(LocalDateTime.now());
    CookingSessionProgressEntity saved = cookingSessionProgressRepository.save(sp);
    return toSession(saved);
  }

  @Transactional
  public CookingDtos.SessionResponse checkpoint(Long id, CookingDtos.CheckpointRequest req) {
    CurrentUser user = authContext.currentUser();
    CookingSessionProgressEntity s = cookingSessionProgressRepository.findById(id)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Session not found", org.springframework.http.HttpStatus.NOT_FOUND));
    if (!s.getUserId().equals(user.id()) && !user.roles().contains("ADMIN")) {
      throw new ApiException("PERMISSION_DENIED", "Not owner", org.springframework.http.HttpStatus.FORBIDDEN);
    }
    s.setCurrentStepNo(req.currentStepNo());
    s.setProgressJson(req.progress());
    s.setLastCheckpointAt(LocalDateTime.now());
    return toSession(cookingSessionProgressRepository.save(s));
  }

  @Transactional
  public CookingDtos.TimerResponse createTimer(Long sessionId, CookingDtos.CreateTimerRequest req) {
    CurrentUser user = authContext.currentUser();
    CookingSessionProgressEntity s = cookingSessionProgressRepository.findById(sessionId)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Session not found", org.springframework.http.HttpStatus.NOT_FOUND));
    if (!s.getUserId().equals(user.id()) && !user.roles().contains("ADMIN")) {
      throw new ApiException("PERMISSION_DENIED", "Not owner", org.springframework.http.HttpStatus.FORBIDDEN);
    }
    CookingTimerEntity timer = new CookingTimerEntity();
    timer.setSessionProgressId(sessionId);
    timer.setStepId(req.stepId());
    timer.setTimerName(req.timerName());
    timer.setDurationSeconds(req.durationSeconds());
    timer.setRemainingSeconds(req.durationSeconds());
    timer.setStatus("RUNNING");
    timer.setReminderIntervalSeconds(req.reminderIntervalSeconds());
    CookingTimerEntity saved = cookingTimerRepository.save(timer);
    return toTimer(saved);
  }

  @Transactional
  public CookingDtos.TimerResponse timerAction(Long timerId, String action) {
    CurrentUser user = authContext.currentUser();
    CookingTimerEntity timer = cookingTimerRepository.findById(timerId)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Timer not found", org.springframework.http.HttpStatus.NOT_FOUND));
    CookingSessionProgressEntity session = cookingSessionProgressRepository.findById(timer.getSessionProgressId())
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Session not found", org.springframework.http.HttpStatus.NOT_FOUND));
    if (!session.getUserId().equals(user.id()) && !user.roles().contains("ADMIN")) {
      throw new ApiException("PERMISSION_DENIED", "Not owner", org.springframework.http.HttpStatus.FORBIDDEN);
    }
    String a = action.toUpperCase();
    if (a.equals("PAUSE")) {
      timer.setStatus("PAUSED");
    } else if (a.equals("RESUME")) {
      timer.setStatus("RUNNING");
    } else if (a.equals("CANCEL")) {
      timer.setStatus("CANCELLED");
    } else {
      throw new ApiException("VALIDATION_ERROR", "Unsupported action", org.springframework.http.HttpStatus.BAD_REQUEST);
    }
    return toTimer(cookingTimerRepository.save(timer));
  }

  @Transactional(readOnly = true)
  public CookingDtos.ResumeResponse resume(Long sessionId) {
    CurrentUser user = authContext.currentUser();
    CookingSessionProgressEntity s = cookingSessionProgressRepository.findById(sessionId)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Session not found", org.springframework.http.HttpStatus.NOT_FOUND));
    if (!s.getUserId().equals(user.id()) && !user.roles().contains("ADMIN")) {
      throw new ApiException("PERMISSION_DENIED", "Not owner", org.springframework.http.HttpStatus.FORBIDDEN);
    }
    List<CookingDtos.TimerResponse> timers = cookingTimerRepository.findBySessionProgressIdAndStatus(sessionId, "RUNNING").stream().map(this::toTimer).toList();
    return new CookingDtos.ResumeResponse(toSession(s), timers);
  }

  private CookingDtos.SessionResponse toSession(CookingSessionProgressEntity s) {
    return new CookingDtos.SessionResponse(s.getId(), s.getProcessId(), s.getUserId(), s.getCurrentStepNo(), s.getStatus(), s.getLastCheckpointAt());
  }

  private CookingDtos.TimerResponse toTimer(CookingTimerEntity t) {
    return new CookingDtos.TimerResponse(t.getId(), t.getSessionProgressId(), t.getStepId(), t.getTimerName(), t.getDurationSeconds(), t.getRemainingSeconds(), t.getStatus());
  }
}
