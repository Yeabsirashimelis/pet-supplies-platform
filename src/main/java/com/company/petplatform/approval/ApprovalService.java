package com.company.petplatform.approval;

import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalService {

  private final ApprovalRequestRepository approvalRequestRepository;
  private final ApprovalActionRepository approvalActionRepository;
  private final AuthContext authContext;

  public ApprovalService(
      ApprovalRequestRepository approvalRequestRepository,
      ApprovalActionRepository approvalActionRepository,
      AuthContext authContext) {
    this.approvalRequestRepository = approvalRequestRepository;
    this.approvalActionRepository = approvalActionRepository;
    this.authContext = authContext;
  }

  @Transactional
  public ApprovalDtos.ApprovalResponse create(ApprovalDtos.CreateApprovalRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    CurrentUser user = authContext.currentUser();
    ApprovalRequestEntity request = new ApprovalRequestEntity();
    request.setRequestNo("APR-" + System.currentTimeMillis());
    request.setRequestType(req.requestType());
    request.setTargetType(req.targetType());
    request.setTargetId(req.targetId());
    request.setInitiatorUserId(user.id());
    request.setReason(req.reason());
    request.setPayloadJson(req.payload());
    request.setStatus("PENDING");
    int minApprovals = isCritical(req.requestType(), req.targetType()) ? 2 : 1;
    request.setRequiredApprovals(Math.max(minApprovals, req.requiredApprovals()));
    request.setApprovedCount(0);
    request.setRejectedCount(0);
    ApprovalRequestEntity saved = approvalRequestRepository.save(request);
    return toResp(saved);
  }

  private boolean isCritical(String requestType, String targetType) {
    String rt = requestType == null ? "" : requestType.toUpperCase();
    String tt = targetType == null ? "" : targetType.toUpperCase();
    return rt.contains("PERMISSION") || rt.contains("CRITICAL") || tt.contains("USER_ROLE") || tt.contains("PERMISSION");
  }

  @Transactional(readOnly = true)
  public ApprovalDtos.ApprovalPage list() {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    return new ApprovalDtos.ApprovalPage(approvalRequestRepository.findAll().stream().map(this::toResp).toList());
  }

  @Transactional(readOnly = true)
  public ApprovalDtos.ApprovalResponse get(Long id) {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    ApprovalRequestEntity request = approvalRequestRepository.findById(id)
        .orElseThrow(() -> new ApiException("APPROVAL_NOT_FOUND", "Approval not found", HttpStatus.NOT_FOUND));
    return toResp(request);
  }

  @Transactional
  public ApprovalDtos.ApprovalResponse approve(Long id, ApprovalDtos.ActionRequest actionRequest) {
    return action(id, "APPROVE", actionRequest);
  }

  @Transactional
  public ApprovalDtos.ApprovalResponse reject(Long id, ApprovalDtos.ActionRequest actionRequest) {
    return action(id, "REJECT", actionRequest);
  }

  @Transactional
  public ApprovalDtos.ApprovalResponse cancel(Long id, ApprovalDtos.ActionRequest actionRequest) {
    CurrentUser user = authContext.currentUser();
    ApprovalRequestEntity req = approvalRequestRepository.findById(id)
        .orElseThrow(() -> new ApiException("APPROVAL_NOT_FOUND", "Approval not found", HttpStatus.NOT_FOUND));
    if (!req.getInitiatorUserId().equals(user.id()) && !user.roles().contains("ADMIN")) {
      throw new ApiException("PERMISSION_DENIED", "Only initiator/admin can cancel", HttpStatus.FORBIDDEN);
    }
    req.setStatus("CANCELLED");
    req.setDecidedAt(LocalDateTime.now());
    ApprovalRequestEntity saved = approvalRequestRepository.save(req);
    ApprovalActionEntity action = new ApprovalActionEntity();
    action.setRequestId(saved.getId());
    action.setApproverUserId(user.id());
    action.setAction("CANCEL");
    action.setCommentText(actionRequest == null ? null : actionRequest.comment());
    approvalActionRepository.save(action);
    return toResp(saved);
  }

  private ApprovalDtos.ApprovalResponse action(Long id, String actionType, ApprovalDtos.ActionRequest actionRequest) {
    authContext.requireAnyRole("ADMIN", "REVIEWER", "MERCHANT");
    CurrentUser user = authContext.currentUser();
    ApprovalRequestEntity req = approvalRequestRepository.findById(id)
        .orElseThrow(() -> new ApiException("APPROVAL_NOT_FOUND", "Approval not found", HttpStatus.NOT_FOUND));
    if (!"PENDING".equals(req.getStatus())) {
      throw new ApiException("CONCURRENT_MODIFICATION", "Approval already decided", HttpStatus.CONFLICT);
    }
    if (req.getInitiatorUserId().equals(user.id())) {
      throw new ApiException("APPROVAL_SELF_APPROVAL_FORBIDDEN", "Initiator cannot self approve/reject", HttpStatus.FORBIDDEN);
    }

    ApprovalActionEntity action = new ApprovalActionEntity();
    action.setRequestId(req.getId());
    action.setApproverUserId(user.id());
    action.setAction(actionType);
    action.setCommentText(actionRequest == null ? null : actionRequest.comment());
    approvalActionRepository.save(action);

    if ("APPROVE".equals(actionType)) {
      req.setApprovedCount(req.getApprovedCount() + 1);
      if (req.getApprovedCount() >= req.getRequiredApprovals()) {
        req.setStatus("APPROVED");
        req.setDecidedAt(LocalDateTime.now());
      }
    } else {
      req.setRejectedCount(req.getRejectedCount() + 1);
      req.setStatus("REJECTED");
      req.setDecidedAt(LocalDateTime.now());
    }

    return toResp(approvalRequestRepository.save(req));
  }

  private ApprovalDtos.ApprovalResponse toResp(ApprovalRequestEntity req) {
    return new ApprovalDtos.ApprovalResponse(
        req.getId(), req.getRequestNo(), req.getRequestType(), req.getTargetType(), req.getTargetId(), req.getInitiatorUserId(), req.getStatus(), req.getRequiredApprovals(), req.getApprovedCount(), req.getRejectedCount(), req.getCreatedAt(), req.getDecidedAt());
  }
}
