package com.company.petplatform.security;

import com.company.petplatform.common.ApiException;
import java.util.Arrays;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {

  public CurrentUser currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof CurrentUser user)) {
      throw new ApiException("AUTH_SESSION_EXPIRED", "Authentication required", HttpStatus.UNAUTHORIZED);
    }
    return user;
  }

  public void requireAnyRole(String... roles) {
    CurrentUser user = currentUser();
    Set<String> roleSet = user.roles();
    boolean matched = Arrays.stream(roles).anyMatch(roleSet::contains);
    if (!matched) {
      throw new ApiException("PERMISSION_DENIED", "Insufficient role", HttpStatus.FORBIDDEN);
    }
  }
}
