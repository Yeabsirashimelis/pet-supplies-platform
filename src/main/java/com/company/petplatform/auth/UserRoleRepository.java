package com.company.petplatform.auth;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleEntity.UserRoleId> {
  List<UserRoleEntity> findByUserId(Long userId);
}
