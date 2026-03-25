package com.company.petplatform.im;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReadCursorRepository extends JpaRepository<MessageReadCursorEntity, Long> {
  Optional<MessageReadCursorEntity> findBySessionIdAndUserId(Long sessionId, Long userId);
}
