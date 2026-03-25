package com.company.petplatform.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "user_role")
@IdClass(UserRoleEntity.UserRoleId.class)
public class UserRoleEntity {
  @Id
  @Column(name = "user_id")
  private Long userId;

  @Id
  @Column(name = "role_id")
  private Long roleId;

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getRoleId() { return roleId; }
  public void setRoleId(Long roleId) { this.roleId = roleId; }

  public static class UserRoleId implements Serializable {
    private Long userId;
    private Long roleId;
    public UserRoleId() {}
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      UserRoleId userRoleId = (UserRoleId) o;
      return Objects.equals(userId, userRoleId.userId) && Objects.equals(roleId, userRoleId.roleId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userId, roleId);
    }
  }
}
