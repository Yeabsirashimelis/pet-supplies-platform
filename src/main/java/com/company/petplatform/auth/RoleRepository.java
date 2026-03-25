package com.company.petplatform.auth;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
  List<RoleEntity> findByIdIn(Collection<Long> ids);
  Optional<RoleEntity> findByCode(String code);
}
