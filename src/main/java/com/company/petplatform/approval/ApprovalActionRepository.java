package com.company.petplatform.approval;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalActionRepository extends JpaRepository<ApprovalActionEntity, Long> {
  List<ApprovalActionEntity> findByRequestId(Long requestId);
}
