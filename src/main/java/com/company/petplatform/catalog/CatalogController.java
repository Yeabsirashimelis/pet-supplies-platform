package com.company.petplatform.catalog;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

  private final CatalogService catalogService;

  public CatalogController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @PostMapping("/brands")
  public CatalogDtos.BrandResponse createBrand(@Valid @RequestBody CatalogDtos.CreateBrandRequest request) {
    return catalogService.createBrand(request);
  }

  @GetMapping("/brands")
  public List<CatalogDtos.BrandResponse> listBrands() {
    return catalogService.listBrands();
  }

  @PostMapping("/categories")
  public CatalogDtos.CategoryResponse createCategory(@Valid @RequestBody CatalogDtos.CreateCategoryRequest request) {
    return catalogService.createCategory(request);
  }

  @GetMapping("/categories/tree")
  public List<CatalogDtos.CategoryResponse> listCategories() {
    return catalogService.listCategories();
  }

  @PostMapping("/attributes")
  public CatalogDtos.AttributeResponse createAttribute(@Valid @RequestBody CatalogDtos.CreateAttributeRequest request) {
    return catalogService.createAttribute(request);
  }

  @GetMapping("/attributes")
  public List<CatalogDtos.AttributeResponse> listAttributes() {
    return catalogService.listAttributes();
  }

  @PostMapping("/products")
  public CatalogDtos.ProductResponse createProduct(@Valid @RequestBody CatalogDtos.CreateProductRequest request) {
    return catalogService.createProduct(request);
  }

  @PutMapping("/products/{id}")
  public CatalogDtos.ProductResponse updateProduct(@PathVariable Long id, @Valid @RequestBody CatalogDtos.CreateProductRequest request) {
    return catalogService.updateProduct(id, request);
  }

  @GetMapping("/products")
  public List<CatalogDtos.ProductResponse> listProducts() {
    return catalogService.listProducts();
  }

  @GetMapping("/products/{id}")
  public CatalogDtos.ProductDetailResponse getProduct(@PathVariable Long id) {
    return catalogService.getProduct(id);
  }

  @PostMapping("/products/{id}/list")
  public CatalogDtos.ProductResponse listProduct(@PathVariable Long id) {
    return catalogService.listProduct(id, true);
  }

  @PostMapping("/products/{id}/delist")
  public CatalogDtos.ProductResponse delistProduct(@PathVariable Long id) {
    return catalogService.listProduct(id, false);
  }

  @PostMapping("/products/{id}/skus")
  public CatalogDtos.SkuResponse createSku(@PathVariable Long id, @Valid @RequestBody CatalogDtos.CreateSkuRequest request) {
    return catalogService.createSku(id, request);
  }

  @GetMapping("/skus/{id}")
  public CatalogDtos.SkuResponse getSku(@PathVariable Long id) {
    return catalogService.getSku(id);
  }

  @PutMapping("/skus/{id}")
  public CatalogDtos.SkuResponse updateSku(@PathVariable Long id, @Valid @RequestBody CatalogDtos.CreateSkuRequest request) {
    return catalogService.updateSku(id, request);
  }

  @DeleteMapping("/skus/{id}")
  public AuthFlag disableSku(@PathVariable Long id) {
    catalogService.disableSku(id);
    return new AuthFlag(true);
  }

  @PostMapping(value = "/products/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ImportResponse importProducts(@RequestParam("file") MultipartFile file) {
    return catalogService.importProducts(file);
  }

  @GetMapping("/products/export")
  public ExportResponse exportProducts() {
    return catalogService.exportProducts();
  }

  public record AuthFlag(boolean success) {
  }

  public record ImportResponse(String jobId, int accepted, int rejected, List<ImportRowResult> rows) {
  }

  public record ImportRowResult(int rowNo, String status, String message) {}

  public record ExportResponse(String filePath) {
  }
}
