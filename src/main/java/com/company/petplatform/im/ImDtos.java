package com.company.petplatform.im;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public class ImDtos {

  public record WsTicketResponse(String wsUrl, String ticket, LocalDateTime expiresAt) {
  }

  public record CreateSessionRequest(@NotBlank String type, @NotEmpty List<Long> memberUserIds) {
  }

  public record SessionResponse(Long id, String sessionNo, String sessionType, String status) {
  }

  public record SendMessageRequest(
      @NotBlank String type,
      @Size(max = 4000)
      String content,
      String imagePath,
      @Pattern(regexp = "^$|image/jpeg|image/png", message = "imageMime must be image/jpeg or image/png")
      String imageMime,
      Integer imageSizeBytes,
      String imageFingerprint) {
  }

  public record MessageResponse(Long id, Long sessionId, Long senderUserId, String messageType, String contentText, Integer foldedCount, Boolean recalledFlag, LocalDateTime createdAt) {
  }

  public record ReadRequest(@NotNull Long lastReadMessageId) {
  }

  public record UnreadResponse(Long sessionId, Integer unreadCount) {
  }

  public record RecallRequest(@NotBlank String reason) {
  }
}
