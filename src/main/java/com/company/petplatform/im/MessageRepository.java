package com.company.petplatform.im;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
  List<MessageEntity> findBySessionIdOrderByIdDesc(Long sessionId);
  Optional<MessageEntity> findTopBySessionIdAndSenderUserIdAndMessageTypeAndContentHashAndCreatedAtAfterOrderByIdDesc(
      Long sessionId,
      Long senderUserId,
      String messageType,
      String contentHash,
      LocalDateTime createdAt);
  Optional<MessageEntity> findTopByImageFingerprintOrderByIdDesc(String imageFingerprint);
  long deleteByExpiresAtBefore(LocalDateTime expiredAt);
}
