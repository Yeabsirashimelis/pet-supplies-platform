package com.company.petplatform.audit;

import com.company.petplatform.security.AuthContext;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

  private final AuditLogRepository auditLogRepository;
  private final AuthContext authContext;

  public AuditService(AuditLogRepository auditLogRepository, AuthContext authContext) {
    this.auditLogRepository = auditLogRepository;
    this.authContext = authContext;
  }

  @Transactional(readOnly = true)
  public AuditDtos.Page query(LocalDateTime from, LocalDateTime to, int page, int size) {
    authContext.requireAnyRole("ADMIN", "REVIEWER");
    LocalDateTime fromTime = from == null ? LocalDateTime.now().minusDays(7) : from;
    LocalDateTime toTime = to == null ? LocalDateTime.now() : to;
    List<AuditLogEntity> rows = auditLogRepository.findByHappenedAtBetweenOrderByHappenedAtDesc(fromTime, toTime);
    List<AuditDtos.Item> items = rows.stream().map(r -> new AuditDtos.Item(
        r.getId(), r.getTraceId(), r.getActorUserId(), r.getActionCode(), r.getTargetType(), r.getTargetId(), r.getResultCode(), r.getHappenedAt())).toList();
    return new AuditDtos.Page(items, page, size, items.size());
  }
}
