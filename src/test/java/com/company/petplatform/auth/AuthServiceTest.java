package com.company.petplatform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserRoleRepository userRoleRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private AuthSessionRepository authSessionRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private AuthContext authContext;

  @InjectMocks private AuthService authService;

  @BeforeEach
  void initProps() {
    ReflectionTestUtils.setField(authService, "maxFailed", 5);
    ReflectionTestUtils.setField(authService, "lockMinutes", 15);
    ReflectionTestUtils.setField(authService, "sessionHours", 12);
  }

  @Test
  void shouldLockAfterFiveFailedAttempts() {
    UserEntity user = new UserEntity();
    user.setUsername("u1");
    user.setPasswordHash("hash");
    user.setDisplayName("U1");
    user.setStatus("ACTIVE");
    user.setFailedLoginCount(4);

    when(userRepository.findByUsername("u1")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong123", "hash")).thenReturn(false);
    when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));

    assertThatThrownBy(() -> authService.login(new AuthDtos.LoginRequest("u1", "wrong123")))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("incorrect");

    assertThat(user.getLockUntil()).isNotNull();
    assertThat(user.getLockUntil()).isAfter(LocalDateTime.now().plusMinutes(14));
    assertThat(user.getFailedLoginCount()).isEqualTo(0);
  }

  @Test
  void shouldRejectLoginDuringLockWindow() {
    UserEntity user = new UserEntity();
    user.setUsername("u1");
    user.setPasswordHash("hash");
    user.setDisplayName("U1");
    user.setStatus("ACTIVE");
    user.setFailedLoginCount(0);
    user.setLockUntil(LocalDateTime.now().plusMinutes(10));

    when(userRepository.findByUsername("u1")).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> authService.login(new AuthDtos.LoginRequest("u1", "abc12345")))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("temporarily locked");
  }
}
