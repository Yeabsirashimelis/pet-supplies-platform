package com.company.petplatform.im;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.company.petplatform.audit.AuditTrailService;
import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ImServiceTest {

  @Mock private ImSessionRepository imSessionRepository;
  @Mock private ImSessionMemberRepository imSessionMemberRepository;
  @Mock private MessageRepository messageRepository;
  @Mock private MessageReadCursorRepository messageReadCursorRepository;
  @Mock private AuthContext authContext;
  @Mock private AuditTrailService auditTrailService;

  @InjectMocks private ImService imService;

  @BeforeEach
  void initProps() {
    ReflectionTestUtils.setField(imService, "duplicateFoldSeconds", 10);
    ReflectionTestUtils.setField(imService, "retentionDays", 180);
    when(authContext.currentUser()).thenReturn(new CurrentUser(1L, "u1", Set.of("BUYER"), 1L));
    ImSessionMemberEntity member = new ImSessionMemberEntity();
    member.setSessionId(1L);
    member.setUserId(1L);
    when(imSessionMemberRepository.findBySessionIdAndUserIdAndLeftAtIsNull(1L, 1L)).thenReturn(Optional.of(member));
  }

  @Test
  void shouldFoldDuplicateTextWithinTenSeconds() {
    MessageEntity existing = new MessageEntity();
    existing.setFoldedCount(1);
    existing.setSessionId(1L);
    existing.setSenderUserId(1L);
    existing.setMessageType("TEXT");
    when(messageRepository.findTopBySessionIdAndSenderUserIdAndMessageTypeAndContentHashAndCreatedAtAfterOrderByIdDesc(
        any(), any(), any(), any(), any())).thenReturn(Optional.of(existing));
    when(messageRepository.save(any(MessageEntity.class))).thenAnswer(i -> i.getArgument(0));

    ImDtos.MessageResponse r = imService.sendMessage(1L, new ImDtos.SendMessageRequest("TEXT", "hello", null, null, null, null));
    assertThat(r.foldedCount()).isEqualTo(2);
  }

  @Test
  void shouldRejectInvalidImageMime() {
    assertThatThrownBy(() -> imService.sendMessage(1L, new ImDtos.SendMessageRequest("IMAGE", null, "/tmp/a.gif", "image/gif", 100, "fp")))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("JPG/PNG");
  }
}
