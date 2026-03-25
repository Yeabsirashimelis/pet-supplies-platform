package com.company.petplatform.cooking;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CookingStepRepository extends JpaRepository<CookingStepEntity, Long> {
  List<CookingStepEntity> findByProcessIdOrderByStepNoAsc(Long processId);
}
