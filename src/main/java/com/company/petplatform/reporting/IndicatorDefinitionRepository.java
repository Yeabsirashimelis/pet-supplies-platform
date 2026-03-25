package com.company.petplatform.reporting;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicatorDefinitionRepository extends JpaRepository<IndicatorDefinitionEntity, Long> {
  Optional<IndicatorDefinitionEntity> findByIndicatorCode(String indicatorCode);
}
