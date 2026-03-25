package com.company.petplatform.security;

import java.util.Set;

public record CurrentUser(Long id, String username, Set<String> roles, Long sessionId) {
}
