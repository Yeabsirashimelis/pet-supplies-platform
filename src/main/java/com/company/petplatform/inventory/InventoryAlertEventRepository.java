package com.company.petplatform.inventory;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryAlertEventRepository extends JpaRepository<InventoryAlertEventEntity, Long> {
  List<InventoryAlertEventEntity> findByStatus(String status);
}
