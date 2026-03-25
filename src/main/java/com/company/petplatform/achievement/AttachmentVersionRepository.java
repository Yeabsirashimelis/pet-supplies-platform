package com.company.petplatform.achievement;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentVersionRepository extends JpaRepository<AttachmentVersionEntity, Long> {
  List<AttachmentVersionEntity> findByBizTypeAndBizIdOrderByVersionAsc(String bizType, String bizId);
}
