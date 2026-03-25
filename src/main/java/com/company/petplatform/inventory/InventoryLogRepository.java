package com.company.petplatform.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryLogRepository extends JpaRepository<InventoryLogEntity, Long> {
}
