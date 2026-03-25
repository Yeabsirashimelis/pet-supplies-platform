package com.company.petplatform.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

public class AuthDtos {

  public record LoginRequest(
      @NotBlank String username,
      @NotBlank String password) {
  }

  public record LoginResponse(
      String sessionToken,
      LocalDateTime expiresAt,
      UserInfo user) {
  }

  public record UserInfo(Long id, String username, Set<String> roles) {
  }

  public record LogoutRequest(boolean allDevices) {
  }

  public record GenericFlagResponse(boolean revoked) {
  }

  public record SessionSummary(Long id, String status, LocalDateTime expiresAt, LocalDateTime lastActivityAt) {
  }

  public record ChangePasswordRequest(
      @NotBlank String oldPassword,
      @NotBlank
      @Size(min = 8)
      @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "must contain letters and numbers")
      String newPassword) {
  }
}
