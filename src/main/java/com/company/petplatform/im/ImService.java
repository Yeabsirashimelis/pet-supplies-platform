package com.company.petplatform.im;

import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import com.company.petplatform.audit.AuditTrailService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImService {

  private final ImSessionRepository imSessionRepository;
  private final ImSessionMemberRepository imSessionMemberRepository;
  private final MessageRepository messageRepository;
  private final MessageReadCursorRepository messageReadCursorRepository;
  private final AuthContext authContext;
  private final AuditTrailService auditTrailService;

  @Value("${app.im.duplicate-fold-seconds:10}")
  private int duplicateFoldSeconds;

  @Value("${app.im.retention-days:180}")
  private int retentionDays;

  public ImService(
      ImSessionRepository imSessionRepository,
      ImSessionMemberRepository imSessionMemberRepository,
      MessageRepository messageRepository,
      MessageReadCursorRepository messageReadCursorRepository,
      AuthContext authContext,
      AuditTrailService auditTrailService) {
    this.imSessionRepository = imSessionRepository;
    this.imSessionMemberRepository = imSessionMemberRepository;
    this.messageRepository = messageRepository;
    this.messageReadCursorRepository = messageReadCursorRepository;
    this.authContext = authContext;
    this.auditTrailService = auditTrailService;
  }

  public ImDtos.WsTicketResponse wsTicket() {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "BUYER", "REVIEWER");
    String ticket = UUID.randomUUID().toString();
    return new ImDtos.WsTicketResponse("wss://localhost:8080/ws", ticket, LocalDateTime.now().plusMinutes(5));
  }

  @Transactional
  public ImDtos.SessionResponse createSession(ImDtos.CreateSessionRequest req) {
    CurrentUser user = authContext.currentUser();
    ImSessionEntity s = new ImSessionEntity();
    s.setSessionNo("S-" + System.currentTimeMillis());
    s.setSessionType(req.type().toUpperCase());
    s.setCreatorUserId(user.id());
    s.setStatus("ACTIVE");
    ImSessionEntity saved = imSessionRepository.save(s);

    ImSessionMemberEntity creator = new ImSessionMemberEntity();
    creator.setSessionId(saved.getId());
    creator.setUserId(user.id());
    creator.setRoleInSession("OWNER");
    imSessionMemberRepository.save(creator);

    for (Long memberUserId : req.memberUserIds()) {
      if (memberUserId.equals(user.id())) {
        continue;
      }
      ImSessionMemberEntity member = new ImSessionMemberEntity();
      member.setSessionId(saved.getId());
      member.setUserId(memberUserId);
      member.setRoleInSession("MEMBER");
      imSessionMemberRepository.save(member);
    }
    return new ImDtos.SessionResponse(saved.getId(), saved.getSessionNo(), saved.getSessionType(), saved.getStatus());
  }

  @Transactional
  public ImDtos.MessageResponse sendMessage(Long sessionId, ImDtos.SendMessageRequest req) {
    CurrentUser user = authContext.currentUser();
    assertMember(sessionId, user.id());

    String type = req.type().toUpperCase();
    if ("IMAGE".equals(type)) {
      if (req.imageMime() == null || (!"image/jpeg".equals(req.imageMime()) && !"image/png".equals(req.imageMime()))) {
        throw new ApiException("FILE_TYPE_NOT_ALLOWED", "Only JPG/PNG images are allowed", HttpStatus.BAD_REQUEST);
      }
      if (req.imageSizeBytes() == null || req.imageSizeBytes() > 2 * 1024 * 1024) {
        throw new ApiException("FILE_TOO_LARGE", "Image exceeds 2MB", HttpStatus.BAD_REQUEST);
      }
      if (req.imageFingerprint() != null && !req.imageFingerprint().isBlank()) {
        var old = messageRepository.findTopByImageFingerprintOrderByIdDesc(req.imageFingerprint());
        if (old.isPresent()) {
          MessageEntity e = old.get();
          MessageEntity m = new MessageEntity();
          m.setMessageNo("M-" + System.nanoTime());
          m.setSessionId(sessionId);
          m.setSenderUserId(user.id());
          m.setMessageType("IMAGE");
          m.setImagePath(e.getImagePath());
          m.setImageMime(e.getImageMime());
          m.setImageSizeBytes(e.getImageSizeBytes());
          m.setImageFingerprint(e.getImageFingerprint());
          m.setFoldedCount(1);
          m.setRecalledFlag(false);
          m.setExpiresAt(LocalDateTime.now().plusDays(retentionDays));
          MessageEntity saved = messageRepository.save(m);
          auditTrailService.append("IM_IMAGE_DEDUP", "MESSAGE", String.valueOf(saved.getId()), "SUCCESS");
          return toMessage(saved);
        }
      }
    }

    if ("TEXT".equals(type) && req.content() != null) {
      String hash = sha256(req.content());
      LocalDateTime threshold = LocalDateTime.now().minusSeconds(duplicateFoldSeconds);
      var existing = messageRepository
          .findTopBySessionIdAndSenderUserIdAndMessageTypeAndContentHashAndCreatedAtAfterOrderByIdDesc(
              sessionId, user.id(), "TEXT", hash, threshold);
      if (existing.isPresent()) {
        MessageEntity e = existing.get();
        e.setFoldedCount((e.getFoldedCount() == null ? 1 : e.getFoldedCount()) + 1);
        MessageEntity saved = messageRepository.save(e);
        return toMessage(saved);
      }
    }

    MessageEntity m = new MessageEntity();
    m.setMessageNo("M-" + System.nanoTime());
    m.setSessionId(sessionId);
    m.setSenderUserId(user.id());
    m.setMessageType(type);
    m.setContentText(req.content());
    m.setContentHash(req.content() == null ? null : sha256(req.content()));
    m.setImagePath(req.imagePath());
    m.setImageMime(req.imageMime());
    m.setImageSizeBytes(req.imageSizeBytes());
    m.setImageFingerprint(req.imageFingerprint());
    m.setFoldedCount(1);
    m.setRecalledFlag(false);
    m.setExpiresAt(LocalDateTime.now().plusDays(retentionDays));
    MessageEntity saved = messageRepository.save(m);

    List<ImSessionMemberEntity> members = imSessionMemberRepository.findBySessionIdAndLeftAtIsNull(sessionId);
    for (ImSessionMemberEntity member : members) {
      if (member.getUserId().equals(user.id())) {
        continue;
      }
      MessageReadCursorEntity cursor = messageReadCursorRepository.findBySessionIdAndUserId(sessionId, member.getUserId())
          .orElseGet(() -> {
            MessageReadCursorEntity c = new MessageReadCursorEntity();
            c.setSessionId(sessionId);
            c.setUserId(member.getUserId());
            c.setUnreadCount(0);
            return c;
          });
      cursor.setUnreadCount((cursor.getUnreadCount() == null ? 0 : cursor.getUnreadCount()) + 1);
      messageReadCursorRepository.save(cursor);
    }
    auditTrailService.append("IM_SEND", "MESSAGE", String.valueOf(saved.getId()), "SUCCESS");
    return toMessage(saved);
  }

  @Transactional
  public ImDtos.MessageResponse recallMessage(Long messageId) {
    CurrentUser user = authContext.currentUser();
    MessageEntity message = messageRepository.findById(messageId)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Message not found", HttpStatus.NOT_FOUND));
    if (!message.getSenderUserId().equals(user.id()) && !user.roles().contains("ADMIN")) {
      throw new ApiException("PERMISSION_DENIED", "Cannot recall this message", HttpStatus.FORBIDDEN);
    }
    message.setRecalledFlag(true);
    message.setRecalledAt(LocalDateTime.now());
    MessageEntity saved = messageRepository.save(message);
    auditTrailService.append("IM_RECALL", "MESSAGE", String.valueOf(saved.getId()), "SUCCESS");
    return toMessage(saved);
  }

  @Transactional
  public ImDtos.UnreadResponse markRead(Long sessionId, ImDtos.ReadRequest req) {
    CurrentUser user = authContext.currentUser();
    assertMember(sessionId, user.id());
    MessageReadCursorEntity cursor = messageReadCursorRepository.findBySessionIdAndUserId(sessionId, user.id())
        .orElseGet(() -> {
          MessageReadCursorEntity c = new MessageReadCursorEntity();
          c.setSessionId(sessionId);
          c.setUserId(user.id());
          c.setUnreadCount(0);
          return c;
        });
    cursor.setLastReadMessageId(req.lastReadMessageId());
    cursor.setUnreadCount(0);
    MessageReadCursorEntity saved = messageReadCursorRepository.save(cursor);
    return new ImDtos.UnreadResponse(sessionId, saved.getUnreadCount());
  }

  @Transactional(readOnly = true)
  public ImDtos.UnreadResponse unread(Long sessionId) {
    CurrentUser user = authContext.currentUser();
    assertMember(sessionId, user.id());
    MessageReadCursorEntity cursor = messageReadCursorRepository.findBySessionIdAndUserId(sessionId, user.id())
        .orElseGet(() -> {
          MessageReadCursorEntity c = new MessageReadCursorEntity();
          c.setSessionId(sessionId);
          c.setUserId(user.id());
          c.setUnreadCount(0);
          return c;
        });
    return new ImDtos.UnreadResponse(sessionId, cursor.getUnreadCount());
  }

  private void assertMember(Long sessionId, Long userId) {
    imSessionMemberRepository.findBySessionIdAndUserIdAndLeftAtIsNull(sessionId, userId)
        .orElseThrow(() -> new ApiException("PERMISSION_DENIED", "Not a session member", HttpStatus.FORBIDDEN));
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (Exception ex) {
      throw new ApiException("SYSTEM_ERROR", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private ImDtos.MessageResponse toMessage(MessageEntity m) {
    return new ImDtos.MessageResponse(m.getId(), m.getSessionId(), m.getSenderUserId(), m.getMessageType(), m.getContentText(), m.getFoldedCount(), m.getRecalledFlag(), m.getCreatedAt());
  }
}
