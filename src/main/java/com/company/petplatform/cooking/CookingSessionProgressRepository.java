package com.company.petplatform.cooking;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookingSessionProgressRepository extends JpaRepository<CookingSessionProgressEntity, Long> {
  List<CookingSessionProgressEntity> findByStatus(String status);
  Optional<CookingSessionProgressEntity> findTopByUserIdAndProcessIdOrderByIdDesc(Long userId, Long processId);
  List<CookingSessionProgressEntity> findByStatusIn(List<String> statuses);
}
