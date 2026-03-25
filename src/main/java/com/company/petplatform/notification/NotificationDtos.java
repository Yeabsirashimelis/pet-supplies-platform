package com.company.petplatform.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class NotificationDtos {

  public record SubscriptionItem(@NotBlank String category, @NotNull Boolean enabled) {
  }

  public record SubscriptionRequest(List<SubscriptionItem> items) {
  }

  public record SubscriptionResponse(Long id, Long userId, String category, Boolean enabled) {
  }

  public record NotificationResponse(Long id, String notificationNo, String category, String title, String contentText, String status, LocalDateTime createdAt, LocalDateTime readAt) {
  }

  public record NotificationsPage(List<NotificationResponse> items) {
  }

  public record ReadAllResponse(int updated) {
  }
}
