package com.company.petplatform.cooking;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookingTimerRepository extends JpaRepository<CookingTimerEntity, Long> {
  List<CookingTimerEntity> findBySessionProgressIdAndStatus(Long sessionProgressId, String status);
}
