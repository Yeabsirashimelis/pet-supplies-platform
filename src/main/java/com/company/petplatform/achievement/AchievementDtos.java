package com.company.petplatform.achievement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AchievementDtos {

  public record CreateAchievementRequest(
      @NotBlank String achievementNo,
      @NotNull Long userId,
      @NotBlank String achievementType,
      @NotBlank String title,
      BigDecimal score,
      @NotBlank String payload,
      @NotNull Integer baseVersion) {
  }

  public record UpdateAchievementRequest(
      @NotBlank String achievementType,
      @NotBlank String title,
      BigDecimal score,
      @NotBlank String payload,
      @NotNull Integer expectedVersion) {
  }

  public record AchievementResponse(
      Long id,
      String achievementNo,
      Long userId,
      String achievementType,
      String title,
      BigDecimal score,
      Integer version,
      String status,
      LocalDateTime createdAt) {
  }

  public record AttachmentRequest(
      @NotBlank String fileName,
      @NotBlank String filePath,
      @NotBlank String mimeType,
      @NotNull Long sizeBytes,
      @NotBlank String fingerprintSha256) {
  }

  public record AttachmentResponse(
      Long id,
      String bizType,
      String bizId,
      String fileName,
      String filePath,
      String mimeType,
      Long sizeBytes,
      String fingerprintSha256,
      Integer version) {
  }

  public record ExportTemplateResponse(String filePath) {
  }

  public record AchievementPage(List<AchievementResponse> items) {
  }
}
