package com.company.petplatform.security;

import com.company.petplatform.auth.AuthSessionEntity;
import com.company.petplatform.auth.AuthSessionRepository;
import com.company.petplatform.auth.RoleEntity;
import com.company.petplatform.auth.RoleRepository;
import com.company.petplatform.auth.UserEntity;
import com.company.petplatform.auth.UserRepository;
import com.company.petplatform.auth.UserRoleEntity;
import com.company.petplatform.auth.UserRoleRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

  private final AuthSessionRepository authSessionRepository;
  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final RoleRepository roleRepository;

  public SessionAuthenticationFilter(
      AuthSessionRepository authSessionRepository,
      UserRepository userRepository,
      UserRoleRepository userRoleRepository,
      RoleRepository roleRepository) {
    this.authSessionRepository = authSessionRepository;
    this.userRepository = userRepository;
    this.userRoleRepository = userRoleRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String auth = request.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring(7).trim();
      Optional<AuthSessionEntity> sessionOpt = authSessionRepository.findBySessionTokenHash(token);
      if (sessionOpt.isPresent()) {
        AuthSessionEntity session = sessionOpt.get();
        if ("ACTIVE".equals(session.getStatus()) && session.getExpiresAt().isAfter(LocalDateTime.now())) {
          Optional<UserEntity> userOpt = userRepository.findById(session.getUserId());
          if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
            List<Long> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).toList();
            Set<String> roles = new HashSet<>();
            if (!roleIds.isEmpty()) {
              List<RoleEntity> roleEntities = roleRepository.findByIdIn(roleIds);
              roleEntities.forEach(r -> roles.add(r.getCode()));
            }
            CurrentUser principal = new CurrentUser(user.getId(), user.getUsername(), roles, session.getId());
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
        }
      }
    }
    filterChain.doFilter(request, response);
  }
}
