package com.company.petplatform.approval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

  @Mock private ApprovalRequestRepository approvalRequestRepository;
  @Mock private ApprovalActionRepository approvalActionRepository;
  @Mock private AuthContext authContext;

  @InjectMocks private ApprovalService approvalService;

  @Test
  void criticalApprovalShouldRequireAtLeastTwoApprovals() {
    when(authContext.currentUser()).thenReturn(new CurrentUser(1L, "admin", Set.of("ADMIN"), 1L));
    when(approvalRequestRepository.save(any(ApprovalRequestEntity.class))).thenAnswer(i -> {
      ApprovalRequestEntity e = i.getArgument(0);
      e.setRequestNo("APR-1");
      return e;
    });

    ApprovalDtos.ApprovalResponse response = approvalService.create(
        new ApprovalDtos.CreateApprovalRequest("PERMISSION_CHANGE", "USER_ROLE", "100", "r", "{}", 1));
    assertThat(response.requiredApprovals()).isEqualTo(2);
  }

  @Test
  void initiatorCannotSelfApprove() {
    when(authContext.currentUser()).thenReturn(new CurrentUser(1L, "admin", Set.of("ADMIN"), 1L));
    ApprovalRequestEntity req = new ApprovalRequestEntity();
    req.setStatus("PENDING");
    req.setInitiatorUserId(1L);
    req.setApprovedCount(0);
    req.setRequiredApprovals(2);
    when(approvalRequestRepository.findById(1L)).thenReturn(Optional.of(req));

    assertThatThrownBy(() -> approvalService.approve(1L, new ApprovalDtos.ActionRequest("ok")))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("Initiator cannot self approve");
  }
}
