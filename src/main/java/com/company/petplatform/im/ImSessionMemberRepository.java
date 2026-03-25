package com.company.petplatform.im;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImSessionMemberRepository extends JpaRepository<ImSessionMemberEntity, Long> {
  List<ImSessionMemberEntity> findBySessionIdAndLeftAtIsNull(Long sessionId);
  Optional<ImSessionMemberEntity> findBySessionIdAndUserIdAndLeftAtIsNull(Long sessionId, Long userId);
}
