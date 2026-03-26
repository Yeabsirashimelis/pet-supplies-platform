package com.company.petplatform.catalog;

import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CatalogService {

  private final BrandRepository brandRepository;
  private final CategoryRepository categoryRepository;
  private final AttributeDefinitionRepository attributeDefinitionRepository;
  private final ProductRepository productRepository;
  private final SkuRepository skuRepository;
  private final AuthContext authContext;

  public CatalogService(
      BrandRepository brandRepository,
      CategoryRepository categoryRepository,
      AttributeDefinitionRepository attributeDefinitionRepository,
      ProductRepository productRepository,
      SkuRepository skuRepository,
      AuthContext authContext) {
    this.brandRepository = brandRepository;
    this.categoryRepository = categoryRepository;
    this.attributeDefinitionRepository = attributeDefinitionRepository;
    this.productRepository = productRepository;
    this.skuRepository = skuRepository;
    this.authContext = authContext;
  }

  @Transactional
  public CatalogDtos.BrandResponse createBrand(CatalogDtos.CreateBrandRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    CurrentUser user = authContext.currentUser();
    BrandEntity entity = new BrandEntity();
    entity.setBrandCode(req.brandCode());
    entity.setBrandName(req.brandName());
    entity.setStatus("ACTIVE");
    entity.setCreatedBy(user.id());
    BrandEntity saved = brandRepository.save(entity);
    return new CatalogDtos.BrandResponse(saved.getId(), saved.getBrandCode(), saved.getBrandName(), saved.getStatus());
  }

  @Transactional(readOnly = true)
  public List<CatalogDtos.BrandResponse> listBrands() {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "BUYER", "REVIEWER");
    return brandRepository.findAll().stream().map(b -> new CatalogDtos.BrandResponse(b.getId(), b.getBrandCode(), b.getBrandName(), b.getStatus())).toList();
  }

  @Transactional
  public CatalogDtos.CategoryResponse createCategory(CatalogDtos.CreateCategoryRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    CurrentUser user = authContext.currentUser();
    int depth = 1;
    String path;
    if (req.parentId() != null) {
      CategoryEntity parent = categoryRepository.findById(req.parentId())
          .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Parent category not found", HttpStatus.NOT_FOUND));
      depth = parent.getDepth() + 1;
      if (depth > 4) {
        throw new ApiException("VALIDATION_ERROR", "Category depth exceeds max 4", HttpStatus.BAD_REQUEST);
      }
      path = parent.getPath() + "/" + req.categoryCode();
    } else {
      path = req.categoryCode();
    }

    CategoryEntity entity = new CategoryEntity();
    entity.setCategoryCode(req.categoryCode());
    entity.setCategoryName(req.categoryName());
    entity.setParentId(req.parentId());
    entity.setDepth((short) depth);
    entity.setPath(path);
    entity.setStatus("ACTIVE");
    entity.setCreatedBy(user.id());
    CategoryEntity saved = categoryRepository.save(entity);
    return new CatalogDtos.CategoryResponse(saved.getId(), saved.getCategoryCode(), saved.getCategoryName(), saved.getParentId(), saved.getDepth(), saved.getPath(), saved.getStatus());
  }

  @Transactional(readOnly = true)
  public List<CatalogDtos.CategoryResponse> listCategories() {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "BUYER", "REVIEWER");
    return categoryRepository.findAll().stream()
        .map(c -> new CatalogDtos.CategoryResponse(c.getId(), c.getCategoryCode(), c.getCategoryName(), c.getParentId(), c.getDepth(), c.getPath(), c.getStatus()))
        .toList();
  }

  @Transactional
  public CatalogDtos.AttributeResponse createAttribute(CatalogDtos.CreateAttributeRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    CurrentUser user = authContext.currentUser();
    AttributeDefinitionEntity entity = new AttributeDefinitionEntity();
    entity.setAttrCode(req.attrCode());
    entity.setAttrName(req.attrName());
    entity.setValueType(req.valueType());
    entity.setScopeLevel(req.scopeLevel());
    entity.setStatus("ACTIVE");
    entity.setCreatedBy(user.id());
    AttributeDefinitionEntity saved = attributeDefinitionRepository.save(entity);
    return new CatalogDtos.AttributeResponse(saved.getId(), saved.getAttrCode(), saved.getAttrName(), saved.getValueType(), saved.getScopeLevel(), saved.getStatus());
  }

  @Transactional(readOnly = true)
  public List<CatalogDtos.AttributeResponse> listAttributes() {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "BUYER", "REVIEWER");
    return attributeDefinitionRepository.findAll().stream()
        .map(a -> new CatalogDtos.AttributeResponse(a.getId(), a.getAttrCode(), a.getAttrName(), a.getValueType(), a.getScopeLevel(), a.getStatus()))
        .toList();
  }

  @Transactional
  public CatalogDtos.ProductResponse createProduct(CatalogDtos.CreateProductRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    CurrentUser user = authContext.currentUser();
    if (productRepository.findByProductCode(req.productCode()).isPresent()) {
      throw new ApiException("CONFLICT_DUPLICATE_KEY", "Duplicate productCode", HttpStatus.CONFLICT);
    }
    if (!brandRepository.existsById(req.brandId())) {
      throw new ApiException("RESOURCE_NOT_FOUND", "Brand not found", HttpStatus.NOT_FOUND);
    }
    if (!categoryRepository.existsById(req.categoryId())) {
      throw new ApiException("RESOURCE_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND);
    }
    ProductEntity p = new ProductEntity();
    p.setProductCode(req.productCode());
    p.setProductName(req.productName());
    p.setMerchantId(user.id());
    p.setBrandId(req.brandId());
    p.setCategoryId(req.categoryId());
    p.setStatus("DRAFT");
    p.setListStatus("DELISTED");
    p.setDescriptionText(req.description());
    ProductEntity saved = productRepository.save(p);
    return toProductResponse(saved);
  }

  @Transactional
  public CatalogDtos.ProductResponse updateProduct(Long id, CatalogDtos.CreateProductRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    CurrentUser user = authContext.currentUser();
    ProductEntity p = productRepository.findById(id)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
    if (!user.roles().contains("ADMIN") && !p.getMerchantId().equals(user.id())) {
      throw new ApiException("PERMISSION_DENIED", "Not owner", HttpStatus.FORBIDDEN);
    }
    p.setProductName(req.productName());
    p.setBrandId(req.brandId());
    p.setCategoryId(req.categoryId());
    p.setDescriptionText(req.description());
    return toProductResponse(productRepository.save(p));
  }

  @Transactional(readOnly = true)
  public CatalogDtos.ProductDetailResponse getProduct(Long id) {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "BUYER", "REVIEWER");
    ProductEntity p = productRepository.findById(id)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
    List<CatalogDtos.SkuResponse> skus = skuRepository.findByProductId(id).stream().map(this::toSkuResponse).toList();
    return new CatalogDtos.ProductDetailResponse(toProductResponse(p), skus);
  }

  @Transactional(readOnly = true)
  public List<CatalogDtos.ProductResponse> listProducts() {
    CurrentUser user = authContext.currentUser();
    authContext.requireAnyRole("ADMIN", "MERCHANT", "BUYER", "REVIEWER");
    List<ProductEntity> list = user.roles().contains("ADMIN") ? productRepository.findAll() : productRepository.findByMerchantId(user.id());
    return list.stream().map(this::toProductResponse).toList();
  }

  @Transactional
  public CatalogDtos.ProductResponse listProduct(Long id, boolean listed) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    CurrentUser user = authContext.currentUser();
    ProductEntity p = productRepository.findById(id)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
    if (!user.roles().contains("ADMIN") && !p.getMerchantId().equals(user.id())) {
      throw new ApiException("PERMISSION_DENIED", "Not owner", HttpStatus.FORBIDDEN);
    }
    p.setListStatus(listed ? "LISTED" : "DELISTED");
    return toProductResponse(productRepository.save(p));
  }

  @Transactional
  public CatalogDtos.SkuResponse createSku(Long productId, CatalogDtos.CreateSkuRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    ProductEntity p = productRepository.findById(productId)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
    CurrentUser user = authContext.currentUser();
    if (!user.roles().contains("ADMIN") && !p.getMerchantId().equals(user.id())) {
      throw new ApiException("PERMISSION_DENIED", "Not owner", HttpStatus.FORBIDDEN);
    }
    if (skuRepository.findBySkuBarcode(req.skuBarcode()).isPresent()) {
      throw new ApiException("CONFLICT_DUPLICATE_KEY", "Duplicate skuBarcode", HttpStatus.CONFLICT);
    }
    SkuEntity sku = new SkuEntity();
    sku.setProductId(productId);
    sku.setSkuCode(req.skuCode());
    sku.setSkuBarcode(req.skuBarcode());
    sku.setSkuName(req.skuName());
    sku.setSalePrice(req.salePrice());
    sku.setStatus("ACTIVE");
    return toSkuResponse(skuRepository.save(sku));
  }

  @Transactional(readOnly = true)
  public CatalogDtos.SkuResponse getSku(Long skuId) {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "BUYER", "REVIEWER");
    SkuEntity sku = skuRepository.findById(skuId)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Sku not found", HttpStatus.NOT_FOUND));
    return toSkuResponse(sku);
  }

  @Transactional
  public CatalogDtos.SkuResponse updateSku(Long skuId, CatalogDtos.CreateSkuRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    SkuEntity sku = skuRepository.findById(skuId)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Sku not found", HttpStatus.NOT_FOUND));
    ProductEntity p = productRepository.findById(sku.getProductId())
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
    CurrentUser user = authContext.currentUser();
    if (!user.roles().contains("ADMIN") && !p.getMerchantId().equals(user.id())) {
      throw new ApiException("PERMISSION_DENIED", "Not owner", HttpStatus.FORBIDDEN);
    }
    sku.setSkuName(req.skuName());
    sku.setSalePrice(req.salePrice());
    return toSkuResponse(skuRepository.save(sku));
  }

  @Transactional
  public void disableSku(Long skuId) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    SkuEntity sku = skuRepository.findById(skuId)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Sku not found", HttpStatus.NOT_FOUND));
    ProductEntity p = productRepository.findById(sku.getProductId())
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
    CurrentUser user = authContext.currentUser();
    if (!user.roles().contains("ADMIN") && !p.getMerchantId().equals(user.id())) {
      throw new ApiException("PERMISSION_DENIED", "Not owner", HttpStatus.FORBIDDEN);
    }
    sku.setStatus("DISABLED");
    skuRepository.save(sku);
  }

  private CatalogDtos.ProductResponse toProductResponse(ProductEntity p) {
    return new CatalogDtos.ProductResponse(
        p.getId(), p.getProductCode(), p.getProductName(), p.getMerchantId(), p.getBrandId(), p.getCategoryId(), p.getStatus(), p.getListStatus(), p.getDescriptionText());
  }

  private CatalogDtos.SkuResponse toSkuResponse(SkuEntity s) {
    return new CatalogDtos.SkuResponse(s.getId(), s.getProductId(), s.getSkuCode(), s.getSkuBarcode(), s.getSkuName(), s.getSalePrice(), s.getStatus());
  }

  @Transactional
  public CatalogController.ImportResponse importProducts(MultipartFile file) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    if (file == null || file.isEmpty()) {
      throw new ApiException("VALIDATION_ERROR", "Import file is empty", HttpStatus.BAD_REQUEST);
    }
    String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
    if (!filename.endsWith(".csv")) {
      throw new ApiException("VALIDATION_ERROR", "Only CSV import is supported", HttpStatus.BAD_REQUEST);
    }

    int accepted = 0;
    int rejected = 0;
    List<CatalogController.ImportRowResult> rows = new ArrayList<>();
    CurrentUser currentUser = authContext.currentUser();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      int rowNo = 0;
      while ((line = reader.readLine()) != null) {
        rowNo++;
        if (rowNo == 1) {
          continue;
        }
        String[] p = line.split(",", -1);
        if (p.length < 8) {
          rejected++;
          rows.add(new CatalogController.ImportRowResult(rowNo, "REJECTED", "Expected 8 columns"));
          continue;
        }
        String productCode = p[0].trim();
        String productName = p[1].trim();
        String brandIdRaw = p[2].trim();
        String categoryIdRaw = p[3].trim();
        String description = p[4].trim();
        String skuCode = p[5].trim();
        String skuBarcode = p[6].trim();
        String salePriceRaw = p[7].trim();

        if (productCode.isBlank() || skuBarcode.isBlank()) {
          rejected++;
          rows.add(new CatalogController.ImportRowResult(rowNo, "REJECTED", "productCode/skuBarcode required"));
          continue;
        }
        if (productRepository.findByProductCode(productCode).isPresent() || skuRepository.findBySkuBarcode(skuBarcode).isPresent()) {
          rejected++;
          rows.add(new CatalogController.ImportRowResult(rowNo, "REJECTED", "Duplicate productCode or skuBarcode"));
          continue;
        }

        Long brandId;
        Long categoryId;
        java.math.BigDecimal salePrice;
        try {
          brandId = Long.parseLong(brandIdRaw);
          categoryId = Long.parseLong(categoryIdRaw);
          salePrice = new java.math.BigDecimal(salePriceRaw);
        } catch (Exception ex) {
          rejected++;
          rows.add(new CatalogController.ImportRowResult(rowNo, "REJECTED", "Invalid numeric values"));
          continue;
        }

        if (!brandRepository.existsById(brandId) || !categoryRepository.existsById(categoryId)) {
          rejected++;
          rows.add(new CatalogController.ImportRowResult(rowNo, "REJECTED", "Brand/category not found"));
          continue;
        }

        ProductEntity product = new ProductEntity();
        product.setProductCode(productCode);
        product.setProductName(productName);
        product.setMerchantId(currentUser.id());
        product.setBrandId(brandId);
        product.setCategoryId(categoryId);
        product.setStatus("DRAFT");
        product.setListStatus("DELISTED");
        product.setDescriptionText(description);
        ProductEntity savedProduct = productRepository.save(product);

        SkuEntity sku = new SkuEntity();
        sku.setProductId(savedProduct.getId());
        sku.setSkuCode(skuCode);
        sku.setSkuBarcode(skuBarcode);
        sku.setSkuName(skuCode);
        sku.setSalePrice(salePrice);
        sku.setStatus("ACTIVE");
        skuRepository.save(sku);

        accepted++;
        rows.add(new CatalogController.ImportRowResult(rowNo, "ACCEPTED", "Imported"));
      }
    } catch (Exception ex) {
      throw new ApiException("VALIDATION_ERROR", "Import parsing failed: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    String jobId = "IMP-" + System.currentTimeMillis();
    return new CatalogController.ImportResponse(jobId, accepted, rejected, rows);
  }

  @Transactional(readOnly = true)
  public CatalogController.ExportResponse exportProducts() {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    return new CatalogController.ExportResponse("/exports/products/products_" + System.currentTimeMillis() + ".csv");
  }
}
