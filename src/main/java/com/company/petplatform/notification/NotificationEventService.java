package com.company.petplatform.notification;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationEventService {

  private final NotificationRepository notificationRepository;
  private final NotificationSubscriptionRepository subscriptionRepository;

  public NotificationEventService(
      NotificationRepository notificationRepository,
      NotificationSubscriptionRepository subscriptionRepository) {
    this.notificationRepository = notificationRepository;
    this.subscriptionRepository = subscriptionRepository;
  }

  @Transactional
  public int publishInternalEvent(String category, String title, String content, List<Long> targetUserIds) {
    int delivered = 0;
    for (Long userId : targetUserIds) {
      boolean enabled = subscriptionRepository.findByUserIdAndCategory(userId, category)
          .map(NotificationSubscriptionEntity::getEnabledFlag)
          .orElse(true);
      if (!enabled) {
        continue;
      }
      NotificationEntity n = new NotificationEntity();
      n.setNotificationNo("N-" + System.nanoTime());
      n.setUserId(userId);
      n.setChannel("INTERNAL");
      n.setCategory(category);
      n.setTitle(title);
      n.setContentText(content);
      n.setStatus("UNREAD");
      notificationRepository.save(n);
      delivered++;
    }
    return delivered;
  }
}
