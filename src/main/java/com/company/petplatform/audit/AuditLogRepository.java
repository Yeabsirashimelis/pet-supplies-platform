package com.company.petplatform.audit;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
  List<AuditLogEntity> findByHappenedAtBetweenOrderByHappenedAtDesc(LocalDateTime from, LocalDateTime to);
  AuditLogEntity findTopByOrderByIdDesc();
}
