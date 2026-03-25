package com.company.petplatform.inventory;

import com.company.petplatform.catalog.ProductEntity;
import com.company.petplatform.catalog.ProductRepository;
import com.company.petplatform.catalog.SkuEntity;
import com.company.petplatform.catalog.SkuRepository;
import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

  private final InventoryRepository inventoryRepository;
  private final InventoryLogRepository inventoryLogRepository;
  private final InventoryAlertEventRepository inventoryAlertEventRepository;
  private final SkuRepository skuRepository;
  private final ProductRepository productRepository;
  private final AuthContext authContext;

  @Value("${app.inventory.default-alert-threshold:10}")
  private int defaultThreshold;

  public InventoryService(
      InventoryRepository inventoryRepository,
      InventoryLogRepository inventoryLogRepository,
      InventoryAlertEventRepository inventoryAlertEventRepository,
      SkuRepository skuRepository,
      ProductRepository productRepository,
      AuthContext authContext) {
    this.inventoryRepository = inventoryRepository;
    this.inventoryLogRepository = inventoryLogRepository;
    this.inventoryAlertEventRepository = inventoryAlertEventRepository;
    this.skuRepository = skuRepository;
    this.productRepository = productRepository;
    this.authContext = authContext;
  }

  @Transactional
  public InventoryDtos.InventoryResponse adjustInventory(Long skuId, InventoryDtos.InventoryAdjustRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    CurrentUser user = authContext.currentUser();
    SkuEntity sku = skuRepository.findById(skuId)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Sku not found", HttpStatus.NOT_FOUND));
    ProductEntity product = productRepository.findById(sku.getProductId())
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Product not found", HttpStatus.NOT_FOUND));
    if (!user.roles().contains("ADMIN") && !product.getMerchantId().equals(user.id())) {
      throw new ApiException("PERMISSION_DENIED", "Not owner", HttpStatus.FORBIDDEN);
    }

    InventoryEntity inv = inventoryRepository.findBySkuId(skuId).orElseGet(() -> {
      InventoryEntity i = new InventoryEntity();
      i.setSkuId(skuId);
      i.setAvailableQty(0);
      i.setReservedQty(0);
      i.setTotalQty(0);
      i.setAlertThreshold(defaultThreshold);
      return i;
    });

    int before = inv.getAvailableQty();
    int after = before + req.delta();
    if (after < 0) {
      throw new ApiException("VALIDATION_ERROR", "Inventory cannot be negative", HttpStatus.BAD_REQUEST);
    }

    inv.setAvailableQty(after);
    inv.setTotalQty(after + inv.getReservedQty());
    InventoryEntity saved = inventoryRepository.save(inv);

    InventoryLogEntity log = new InventoryLogEntity();
    log.setSkuId(skuId);
    log.setChangeType(req.changeType());
    log.setChangeQty(req.delta());
    log.setBeforeQty(before);
    log.setAfterQty(after);
    log.setReferenceType(req.referenceType());
    log.setReferenceId(req.referenceId());
    log.setOperatorUserId(user.id());
    inventoryLogRepository.save(log);

    if (saved.getAvailableQty() <= saved.getAlertThreshold()) {
      InventoryAlertEventEntity alert = new InventoryAlertEventEntity();
      alert.setSkuId(saved.getSkuId());
      alert.setCurrentStock(saved.getAvailableQty());
      alert.setThresholdValue(saved.getAlertThreshold());
      alert.setStatus("OPEN");
      inventoryAlertEventRepository.save(alert);
    }

    return toResponse(saved);
  }

  @Transactional
  public InventoryDtos.InventoryResponse setThreshold(Long skuId, InventoryDtos.ThresholdRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    if (req.alertThreshold() < 0) {
      throw new ApiException("VALIDATION_ERROR", "Threshold must be >= 0", HttpStatus.BAD_REQUEST);
    }
    InventoryEntity inv = inventoryRepository.findBySkuId(skuId).orElseGet(() -> {
      InventoryEntity i = new InventoryEntity();
      i.setSkuId(skuId);
      i.setAvailableQty(0);
      i.setReservedQty(0);
      i.setTotalQty(0);
      i.setAlertThreshold(defaultThreshold);
      return i;
    });
    inv.setAlertThreshold(req.alertThreshold());
    return toResponse(inventoryRepository.save(inv));
  }

  @Transactional(readOnly = true)
  public InventoryDtos.AlertsPage listAlerts(String status) {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    String queryStatus = status == null || status.isBlank() ? "OPEN" : status;
    List<InventoryDtos.AlertResponse> items = inventoryAlertEventRepository.findByStatus(queryStatus).stream()
        .map(a -> new InventoryDtos.AlertResponse(a.getId(), a.getSkuId(), a.getCurrentStock(), a.getThresholdValue(), a.getStatus(), a.getTriggeredAt()))
        .toList();
    return new InventoryDtos.AlertsPage(items);
  }

  @Transactional
  public InventoryDtos.AlertResponse handleAlert(Long id, InventoryDtos.AlertHandleRequest req) {
    authContext.requireAnyRole("ADMIN", "MERCHANT");
    CurrentUser user = authContext.currentUser();
    InventoryAlertEventEntity event = inventoryAlertEventRepository.findById(id)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Alert not found", HttpStatus.NOT_FOUND));
    String action = req.action().toUpperCase();
    if (!action.equals("IGNORE") && !action.equals("RECOVER")) {
      throw new ApiException("VALIDATION_ERROR", "action must be IGNORE or RECOVER", HttpStatus.BAD_REQUEST);
    }
    event.setStatus(action.equals("IGNORE") ? "IGNORED" : "RECOVERED");
    event.setHandledBy(user.id());
    event.setHandledAt(LocalDateTime.now());
    InventoryAlertEventEntity saved = inventoryAlertEventRepository.save(event);
    return new InventoryDtos.AlertResponse(saved.getId(), saved.getSkuId(), saved.getCurrentStock(), saved.getThresholdValue(), saved.getStatus(), saved.getTriggeredAt());
  }

  private InventoryDtos.InventoryResponse toResponse(InventoryEntity e) {
    return new InventoryDtos.InventoryResponse(e.getSkuId(), e.getAvailableQty(), e.getReservedQty(), e.getTotalQty(), e.getAlertThreshold());
  }
}
