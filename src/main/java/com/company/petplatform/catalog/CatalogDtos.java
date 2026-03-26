package com.company.petplatform.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public class CatalogDtos {

  public record CreateBrandRequest(
      @NotBlank String brandCode,
      @NotBlank String brandName) {
  }

  public record BrandResponse(Long id, String brandCode, String brandName, String status) {
  }

  public record CreateCategoryRequest(
      @NotBlank String categoryCode,
      @NotBlank String categoryName,
      Long parentId) {
  }

  public record CategoryResponse(Long id, String categoryCode, String categoryName, Long parentId, Short depth, String path, String status) {
  }

  public record CreateAttributeRequest(
      @NotBlank String attrCode,
      @NotBlank String attrName,
      @NotBlank String valueType,
      @NotBlank String scopeLevel) {
  }

  public record AttributeResponse(Long id, String attrCode, String attrName, String valueType, String scopeLevel, String status) {
  }

  public record CreateProductRequest(
      @NotBlank String productCode,
      @NotBlank String productName,
      @NotNull Long brandId,
      @NotNull Long categoryId,
      @Size(max = 5000) String description) {
  }

  public record ProductResponse(Long id, String productCode, String productName, Long merchantId, Long brandId, Long categoryId, String status, String listStatus, String description) {
  }

  public record CreateSkuRequest(
      @NotBlank String skuCode,
      @NotBlank String skuBarcode,
      @NotBlank String skuName,
      @NotNull @DecimalMin("0.0") BigDecimal salePrice) {
  }

  public record SkuResponse(Long id, Long productId, String skuCode, String skuBarcode, String skuName, BigDecimal salePrice, String status) {
  }

  public record ProductDetailResponse(ProductResponse product, List<SkuResponse> skus) {
  }
}
