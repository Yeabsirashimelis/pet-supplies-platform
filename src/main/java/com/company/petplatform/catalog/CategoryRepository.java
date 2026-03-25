package com.company.petplatform.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
  List<CategoryEntity> findByStatus(String status);
}
