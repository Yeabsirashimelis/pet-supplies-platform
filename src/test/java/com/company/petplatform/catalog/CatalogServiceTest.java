package com.company.petplatform.catalog;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
class CatalogServiceTest {

  @Mock private BrandRepository brandRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private AttributeDefinitionRepository attributeDefinitionRepository;
  @Mock private ProductRepository productRepository;
  @Mock private SkuRepository skuRepository;
  @Mock private AuthContext authContext;

  @InjectMocks private CatalogService catalogService;

  @Test
  void shouldRejectCategoryDepthBeyondFour() {
    when(authContext.currentUser()).thenReturn(new CurrentUser(1L, "m1", Set.of("MERCHANT"), 1L));
    CategoryEntity parent = new CategoryEntity();
    parent.setDepth((short) 4);
    parent.setPath("L1/L2/L3/L4");
    when(categoryRepository.findById(10L)).thenReturn(Optional.of(parent));

    assertThatThrownBy(() -> catalogService.createCategory(new CatalogDtos.CreateCategoryRequest("L5", "Level5", 10L)))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("max 4");
  }

  @Test
  void shouldRejectDuplicateSkuBarcode() {
    when(authContext.currentUser()).thenReturn(new CurrentUser(1L, "m1", Set.of("MERCHANT"), 1L));
    ProductEntity p = new ProductEntity();
    p.setMerchantId(1L);
    when(productRepository.findById(1L)).thenReturn(Optional.of(p));

    SkuEntity existing = new SkuEntity();
    when(skuRepository.findBySkuBarcode("690123")).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> catalogService.createSku(1L, new CatalogDtos.CreateSkuRequest("SKU1", "690123", "Name", java.math.BigDecimal.TEN)))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("Duplicate skuBarcode");
  }
}
