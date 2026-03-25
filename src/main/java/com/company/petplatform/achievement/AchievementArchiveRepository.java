package com.company.petplatform.achievement;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementArchiveRepository extends JpaRepository<AchievementArchiveEntity, Long> {
  List<AchievementArchiveEntity> findByAchievementNoOrderByVersionAsc(String achievementNo);
  Optional<AchievementArchiveEntity> findTopByAchievementNoOrderByVersionDesc(String achievementNo);
  List<AchievementArchiveEntity> findByUserId(Long userId);
}
