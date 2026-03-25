package com.company.petplatform.notification;

import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationSubscriptionRepository notificationSubscriptionRepository;
  private final AuthContext authContext;

  public NotificationService(
      NotificationRepository notificationRepository,
      NotificationSubscriptionRepository notificationSubscriptionRepository,
      AuthContext authContext) {
    this.notificationRepository = notificationRepository;
    this.notificationSubscriptionRepository = notificationSubscriptionRepository;
    this.authContext = authContext;
  }

  @Transactional
  public List<NotificationDtos.SubscriptionResponse> upsertSubscriptions(NotificationDtos.SubscriptionRequest req) {
    CurrentUser user = authContext.currentUser();
    List<NotificationDtos.SubscriptionResponse> responses = new ArrayList<>();
    for (NotificationDtos.SubscriptionItem item : req.items()) {
      NotificationSubscriptionEntity entity = notificationSubscriptionRepository
          .findByUserIdAndCategory(user.id(), item.category())
          .orElseGet(() -> {
            NotificationSubscriptionEntity n = new NotificationSubscriptionEntity();
            n.setUserId(user.id());
            n.setCategory(item.category());
            return n;
          });
      entity.setEnabledFlag(item.enabled());
      NotificationSubscriptionEntity saved = notificationSubscriptionRepository.save(entity);
      responses.add(new NotificationDtos.SubscriptionResponse(saved.getId(), saved.getUserId(), saved.getCategory(), saved.getEnabledFlag()));
    }
    return responses;
  }

  @Transactional(readOnly = true)
  public NotificationDtos.NotificationsPage list() {
    CurrentUser user = authContext.currentUser();
    List<NotificationDtos.NotificationResponse> items = notificationRepository.findByUserId(user.id()).stream()
        .map(this::toResponse)
        .toList();
    return new NotificationDtos.NotificationsPage(items);
  }

  @Transactional
  public NotificationDtos.NotificationResponse markRead(Long id) {
    CurrentUser user = authContext.currentUser();
    NotificationEntity n = notificationRepository.findById(id)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Notification not found", HttpStatus.NOT_FOUND));
    if (!n.getUserId().equals(user.id()) && !user.roles().contains("ADMIN")) {
      throw new ApiException("PERMISSION_DENIED", "Not owner", HttpStatus.FORBIDDEN);
    }
    n.setStatus("READ");
    n.setReadAt(LocalDateTime.now());
    return toResponse(notificationRepository.save(n));
  }

  @Transactional
  public NotificationDtos.ReadAllResponse readAll() {
    CurrentUser user = authContext.currentUser();
    List<NotificationEntity> list = notificationRepository.findByUserId(user.id());
    int updated = 0;
    for (NotificationEntity n : list) {
      if (!"READ".equals(n.getStatus())) {
        n.setStatus("READ");
        n.setReadAt(LocalDateTime.now());
        updated++;
      }
    }
    notificationRepository.saveAll(list);
    return new NotificationDtos.ReadAllResponse(updated);
  }

  private NotificationDtos.NotificationResponse toResponse(NotificationEntity n) {
    return new NotificationDtos.NotificationResponse(n.getId(), n.getNotificationNo(), n.getCategory(), n.getTitle(), n.getContentText(), n.getStatus(), n.getCreatedAt(), n.getReadAt());
  }
}
