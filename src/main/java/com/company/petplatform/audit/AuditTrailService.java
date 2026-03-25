package com.company.petplatform.audit;

import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditTrailService {

  private final AuditLogRepository auditLogRepository;
  private final AuthContext authContext;
  private final HttpServletRequest request;

  public AuditTrailService(AuditLogRepository auditLogRepository, AuthContext authContext, HttpServletRequest request) {
    this.auditLogRepository = auditLogRepository;
    this.authContext = authContext;
    this.request = request;
  }

  @Transactional
  public void append(String actionCode, String targetType, String targetId, String resultCode) {
    AuditLogEntity prev = auditLogRepository.findTopByOrderByIdDesc();
    String prevHash = prev == null ? null : prev.getHashSelf();

    Long actorId = null;
    try {
      CurrentUser user = authContext.currentUser();
      actorId = user.id();
    } catch (Exception ignored) {
    }

    AuditLogEntity log = new AuditLogEntity();
    log.setTraceId(java.util.UUID.randomUUID().toString());
    log.setActorUserId(actorId);
    log.setActionCode(actionCode);
    log.setTargetType(targetType);
    log.setTargetId(targetId);
    log.setResultCode(resultCode);
    log.setRequestId(request.getHeader("X-Request-Id"));
    log.setHashPrev(prevHash);
    log.setHappenedAt(LocalDateTime.now());
    String material = (prevHash == null ? "GENESIS" : prevHash) + "|" + actionCode + "|" + targetType + "|" + targetId + "|" + resultCode + "|" + log.getHappenedAt();
    log.setHashSelf(sha256(material));
    auditLogRepository.save(log);
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception ex) {
      throw new IllegalStateException(ex.getMessage(), ex);
    }
  }
}
