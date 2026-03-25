package com.company.petplatform.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuRepository extends JpaRepository<SkuEntity, Long> {
  Optional<SkuEntity> findBySkuBarcode(String skuBarcode);
  List<SkuEntity> findByProductId(Long productId);
}
