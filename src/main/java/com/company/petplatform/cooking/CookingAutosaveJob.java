package com.company.petplatform.cooking;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CookingAutosaveJob {

  private final CookingSessionProgressRepository cookingSessionProgressRepository;

  public CookingAutosaveJob(CookingSessionProgressRepository cookingSessionProgressRepository) {
    this.cookingSessionProgressRepository = cookingSessionProgressRepository;
  }

  @Scheduled(fixedDelayString = "${app.cooking.autosave-seconds:30}000")
  @Transactional
  public void autosaveRunningSessions() {
    List<CookingSessionProgressEntity> sessions =
        cookingSessionProgressRepository.findByStatusIn(List.of("RUNNING", "PAUSED"));
    LocalDateTime now = LocalDateTime.now();
    for (CookingSessionProgressEntity session : sessions) {
      session.setLastCheckpointAt(now);
    }
    cookingSessionProgressRepository.saveAll(sessions);
  }
}
