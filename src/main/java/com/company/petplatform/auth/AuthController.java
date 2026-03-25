package com.company.petplatform.auth;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/logout")
  public AuthDtos.GenericFlagResponse logout(@RequestBody(required = false) AuthDtos.LogoutRequest request) {
    boolean allDevices = request != null && request.allDevices();
    return authService.logout(allDevices);
  }

  @GetMapping("/sessions")
  public List<AuthDtos.SessionSummary> sessions() {
    return authService.sessions();
  }

  @DeleteMapping("/sessions/{sessionId}")
  public AuthDtos.GenericFlagResponse revokeSession(@PathVariable Long sessionId) {
    return authService.revokeSession(sessionId);
  }

  @PostMapping("/password/change")
  public AuthDtos.GenericFlagResponse changePassword(@Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
    authService.changePassword(request);
    return new AuthDtos.GenericFlagResponse(true);
  }
}
