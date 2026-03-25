package com.company.petplatform.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, Long> {
  Optional<AuthSessionEntity> findBySessionTokenHash(String sessionTokenHash);
  List<AuthSessionEntity> findByUserIdAndStatus(Long userId, String status);
}
