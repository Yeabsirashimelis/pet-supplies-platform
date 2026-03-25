package com.company.petplatform.config;

import com.company.petplatform.audit.AuditTrailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestAuditFilter extends OncePerRequestFilter {

  private final AuditTrailService auditTrailService;

  public RequestAuditFilter(AuditTrailService auditTrailService) {
    this.auditTrailService = auditTrailService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    filterChain.doFilter(request, response);
    String uri = request.getRequestURI();
    if (uri.startsWith("/api/v1") && !uri.contains("/audit/logs")) {
      String action = request.getMethod() + " " + uri;
      String result = response.getStatus() >= 400 ? "FAILED" : "SUCCESS";
      try {
        auditTrailService.append(action, "HTTP", uri, result);
      } catch (Exception ignored) {
      }
    }
  }
}
