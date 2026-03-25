package com.company.petplatform.auth;

import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final RoleRepository roleRepository;
  private final AuthSessionRepository authSessionRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthContext authContext;

  @Value("${app.security.session-hours:12}")
  private int sessionHours;

  @Value("${app.security.max-failed-logins:5}")
  private int maxFailed;

  @Value("${app.security.lock-minutes:15}")
  private int lockMinutes;

  public AuthService(
      UserRepository userRepository,
      UserRoleRepository userRoleRepository,
      RoleRepository roleRepository,
      AuthSessionRepository authSessionRepository,
      PasswordEncoder passwordEncoder,
      AuthContext authContext) {
    this.userRepository = userRepository;
    this.userRoleRepository = userRoleRepository;
    this.roleRepository = roleRepository;
    this.authSessionRepository = authSessionRepository;
    this.passwordEncoder = passwordEncoder;
    this.authContext = authContext;
  }

  @Transactional
  public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
    UserEntity user = userRepository.findByUsername(req.username())
        .orElseThrow(() -> new ApiException("AUTH_INVALID_CREDENTIALS", "Username or password is incorrect", HttpStatus.UNAUTHORIZED));

    if (user.getLockUntil() != null && user.getLockUntil().isAfter(LocalDateTime.now())) {
      throw new ApiException("AUTH_ACCOUNT_LOCKED", "Account is temporarily locked", HttpStatus.LOCKED);
    }

    if (!"ACTIVE".equals(user.getStatus())) {
      throw new ApiException("AUTH_ACCOUNT_DISABLED", "Account disabled", HttpStatus.FORBIDDEN);
    }

    if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
      int failed = user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount();
      failed++;
      user.setFailedLoginCount(failed);
      if (failed >= maxFailed) {
        user.setLockUntil(LocalDateTime.now().plusMinutes(lockMinutes));
        user.setFailedLoginCount(0);
      }
      userRepository.save(user);
      log.warn("Login failed for user={}", com.company.petplatform.common.LogMaskingUtil.mask(req.username()));
      throw new ApiException("AUTH_INVALID_CREDENTIALS", "Username or password is incorrect", HttpStatus.UNAUTHORIZED);
    }

    user.setFailedLoginCount(0);
    user.setLockUntil(null);
    user.setLastLoginAt(LocalDateTime.now());
    userRepository.save(user);

    List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
    List<Long> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).toList();
    Set<String> roles = roleIds.isEmpty() ? Set.of() : roleRepository.findByIdIn(roleIds).stream().map(RoleEntity::getCode).collect(java.util.stream.Collectors.toSet());

    String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    AuthSessionEntity session = new AuthSessionEntity();
    session.setSessionTokenHash(token);
    session.setUserId(user.getId());
    session.setStatus("ACTIVE");
    session.setLastActivityAt(LocalDateTime.now());
    session.setExpiresAt(LocalDateTime.now().plusHours(sessionHours));
    authSessionRepository.save(session);
    log.info("Login success userId={} sessionId={}", user.getId(), session.getId());

    return new AuthDtos.LoginResponse(token, session.getExpiresAt(), new AuthDtos.UserInfo(user.getId(), user.getUsername(), roles));
  }

  @Transactional
  public AuthDtos.GenericFlagResponse logout(boolean allDevices) {
    CurrentUser user = authContext.currentUser();
    if (allDevices) {
      List<AuthSessionEntity> sessions = authSessionRepository.findByUserIdAndStatus(user.id(), "ACTIVE");
      LocalDateTime now = LocalDateTime.now();
      sessions.forEach(s -> {
        s.setStatus("REVOKED");
        s.setRevokedAt(now);
      });
      authSessionRepository.saveAll(sessions);
    } else {
      AuthSessionEntity session = authSessionRepository.findById(user.sessionId())
          .orElseThrow(() -> new ApiException("AUTH_SESSION_EXPIRED", "Session not found", HttpStatus.UNAUTHORIZED));
      session.setStatus("REVOKED");
      session.setRevokedAt(LocalDateTime.now());
      authSessionRepository.save(session);
    }
    return new AuthDtos.GenericFlagResponse(true);
  }

  @Transactional(readOnly = true)
  public List<AuthDtos.SessionSummary> sessions() {
    CurrentUser user = authContext.currentUser();
    return authSessionRepository.findByUserIdAndStatus(user.id(), "ACTIVE").stream()
        .map(s -> new AuthDtos.SessionSummary(s.getId(), s.getStatus(), s.getExpiresAt(), s.getLastActivityAt()))
        .toList();
  }

  @Transactional
  public AuthDtos.GenericFlagResponse revokeSession(Long sessionId) {
    CurrentUser user = authContext.currentUser();
    AuthSessionEntity session = authSessionRepository.findById(sessionId)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Session not found", HttpStatus.NOT_FOUND));
    if (!session.getUserId().equals(user.id()) && !user.roles().contains("ADMIN")) {
      throw new ApiException("PERMISSION_DENIED", "Cannot revoke this session", HttpStatus.FORBIDDEN);
    }
    session.setStatus("REVOKED");
    session.setRevokedAt(LocalDateTime.now());
    authSessionRepository.save(session);
    return new AuthDtos.GenericFlagResponse(true);
  }

  @Transactional
  public void changePassword(AuthDtos.ChangePasswordRequest req) {
    CurrentUser current = authContext.currentUser();
    UserEntity user = userRepository.findById(current.id())
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));
    if (!passwordEncoder.matches(req.oldPassword(), user.getPasswordHash())) {
      throw new ApiException("AUTH_INVALID_CREDENTIALS", "Old password is incorrect", HttpStatus.UNAUTHORIZED);
    }
    user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
    userRepository.save(user);
    log.info("Password changed userId={}", user.getId());
  }
}
