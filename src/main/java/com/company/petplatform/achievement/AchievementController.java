package com.company.petplatform.achievement;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/achievements")
public class AchievementController {

  private final AchievementService achievementService;

  public AchievementController(AchievementService achievementService) {
    this.achievementService = achievementService;
  }

  @PostMapping
  public AchievementDtos.AchievementResponse create(@Valid @RequestBody AchievementDtos.CreateAchievementRequest request) {
    return achievementService.create(request);
  }

  @PutMapping("/{achievementNo}")
  public AchievementDtos.AchievementResponse update(
      @PathVariable String achievementNo,
      @Valid @RequestBody AchievementDtos.UpdateAchievementRequest request) {
    return achievementService.update(achievementNo, request);
  }

  @GetMapping
  public AchievementDtos.AchievementPage list(@RequestParam(required = false) Long userId) {
    return achievementService.list(userId);
  }

  @GetMapping("/{achievementNo}/versions")
  public List<AchievementDtos.AchievementResponse> versions(@PathVariable String achievementNo) {
    return achievementService.versions(achievementNo);
  }

  @PostMapping("/{achievementNo}/attachments")
  public AchievementDtos.AttachmentResponse attachment(
      @PathVariable String achievementNo,
      @Valid @RequestBody AchievementDtos.AttachmentRequest request) {
    return achievementService.addAttachment(achievementNo, request);
  }

  @GetMapping("/templates/export")
  public AchievementDtos.ExportTemplateResponse exportTemplate() {
    return achievementService.exportTemplate();
  }
}
