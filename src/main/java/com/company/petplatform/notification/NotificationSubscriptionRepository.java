package com.company.petplatform.notification;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSubscriptionRepository extends JpaRepository<NotificationSubscriptionEntity, Long> {
  List<NotificationSubscriptionEntity> findByUserId(Long userId);
  Optional<NotificationSubscriptionEntity> findByUserIdAndCategory(Long userId, String category);
}
