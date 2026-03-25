package com.company.petplatform.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
  Optional<ProductEntity> findByProductCode(String productCode);
  List<ProductEntity> findByMerchantId(Long merchantId);
}
