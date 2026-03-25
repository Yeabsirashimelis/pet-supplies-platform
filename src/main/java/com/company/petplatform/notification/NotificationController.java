package com.company.petplatform.notification;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final NotificationService notificationService;
  private final NotificationEventService notificationEventService;

  public NotificationController(NotificationService notificationService, NotificationEventService notificationEventService) {
    this.notificationService = notificationService;
    this.notificationEventService = notificationEventService;
  }

  @PutMapping("/subscriptions")
  public List<NotificationDtos.SubscriptionResponse> subscribe(@Valid @RequestBody NotificationDtos.SubscriptionRequest request) {
    return notificationService.upsertSubscriptions(request);
  }

  @GetMapping
  public NotificationDtos.NotificationsPage list() {
    return notificationService.list();
  }

  @PostMapping("/{id}/read")
  public NotificationDtos.NotificationResponse read(@PathVariable Long id) {
    return notificationService.markRead(id);
  }

  @PostMapping("/read-all")
  public NotificationDtos.ReadAllResponse readAll() {
    return notificationService.readAll();
  }

  @PostMapping("/events/publish")
  public PublishResult publish(
      @RequestParam String category,
      @RequestParam String title,
      @RequestParam String content,
      @RequestParam List<Long> userIds) {
    int delivered = notificationEventService.publishInternalEvent(category, title, content, userIds);
    return new PublishResult(delivered);
  }

  public record PublishResult(int delivered) {}
}
