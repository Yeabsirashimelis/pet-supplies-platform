package com.company.petplatform.audit;

import java.time.LocalDateTime;
import java.util.List;

public class AuditDtos {
  public record Item(Long id, String traceId, Long actorUserId, String actionCode, String targetType, String targetId, String resultCode, LocalDateTime happenedAt) {
  }

  public record Page(List<Item> items, int page, int size, long total) {
  }
}
