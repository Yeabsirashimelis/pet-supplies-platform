package com.company.petplatform.achievement;

import com.company.petplatform.common.ApiException;
import com.company.petplatform.security.AuthContext;
import com.company.petplatform.security.CurrentUser;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AchievementService {

  private final AchievementArchiveRepository achievementArchiveRepository;
  private final AttachmentVersionRepository attachmentVersionRepository;
  private final AuthContext authContext;

  public AchievementService(
      AchievementArchiveRepository achievementArchiveRepository,
      AttachmentVersionRepository attachmentVersionRepository,
      AuthContext authContext) {
    this.achievementArchiveRepository = achievementArchiveRepository;
    this.attachmentVersionRepository = attachmentVersionRepository;
    this.authContext = authContext;
  }

  @Transactional
  public AchievementDtos.AchievementResponse create(AchievementDtos.CreateAchievementRequest req) {
    CurrentUser user = authContext.currentUser();
    if (!user.roles().contains("ADMIN") && !user.roles().contains("MERCHANT") && !user.roles().contains("REVIEWER") && !req.userId().equals(user.id())) {
      throw new ApiException("PERMISSION_DENIED", "Cannot create for other user", HttpStatus.FORBIDDEN);
    }
    int latest = achievementArchiveRepository.findTopByAchievementNoOrderByVersionDesc(req.achievementNo())
        .map(AchievementArchiveEntity::getVersion)
        .orElse(0);
    if (req.baseVersion() != latest) {
      throw new ApiException("CONCURRENT_MODIFICATION", "Version mismatch", HttpStatus.CONFLICT);
    }
    AchievementArchiveEntity a = new AchievementArchiveEntity();
    a.setAchievementNo(req.achievementNo());
    a.setUserId(req.userId());
    a.setAchievementType(req.achievementType());
    a.setTitle(req.title());
    a.setScore(req.score());
    a.setPayloadJson(req.payload());
    a.setVersion(latest + 1);
    a.setStatus("ACTIVE");
    a.setCreatedBy(user.id());
    AchievementArchiveEntity saved = achievementArchiveRepository.save(a);
    return toResp(saved);
  }

  @Transactional
  public AchievementDtos.AchievementResponse update(String achievementNo, AchievementDtos.UpdateAchievementRequest req) {
    CurrentUser user = authContext.currentUser();
    AchievementArchiveEntity latest = achievementArchiveRepository.findTopByAchievementNoOrderByVersionDesc(achievementNo)
        .orElseThrow(() -> new ApiException("RESOURCE_NOT_FOUND", "Achievement not found", HttpStatus.NOT_FOUND));
    if (!user.roles().contains("ADMIN") && !user.roles().contains("MERCHANT") && !user.roles().contains("REVIEWER")) {
      throw new ApiException("PERMISSION_DENIED", "Insufficient role", HttpStatus.FORBIDDEN);
    }
    if (!latest.getVersion().equals(req.expectedVersion())) {
      throw new ApiException("CONCURRENT_MODIFICATION", "Version mismatch", HttpStatus.CONFLICT);
    }
    AchievementArchiveEntity newVersion = new AchievementArchiveEntity();
    newVersion.setAchievementNo(achievementNo);
    newVersion.setUserId(latest.getUserId());
    newVersion.setAchievementType(req.achievementType());
    newVersion.setTitle(req.title());
    newVersion.setScore(req.score());
    newVersion.setPayloadJson(req.payload());
    newVersion.setVersion(latest.getVersion() + 1);
    newVersion.setStatus("ACTIVE");
    newVersion.setCreatedBy(user.id());
    AchievementArchiveEntity saved = achievementArchiveRepository.save(newVersion);
    return toResp(saved);
  }

  @Transactional(readOnly = true)
  public AchievementDtos.AchievementPage list(Long userId) {
    CurrentUser user = authContext.currentUser();
    Long target = user.roles().contains("ADMIN") || user.roles().contains("REVIEWER") ? userId : user.id();
    if (target == null) {
      target = user.id();
    }
    List<AchievementDtos.AchievementResponse> items = achievementArchiveRepository.findByUserId(target).stream().map(this::toResp).toList();
    return new AchievementDtos.AchievementPage(items);
  }

  @Transactional(readOnly = true)
  public List<AchievementDtos.AchievementResponse> versions(String achievementNo) {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "BUYER", "REVIEWER");
    return achievementArchiveRepository.findByAchievementNoOrderByVersionAsc(achievementNo).stream().map(this::toResp).toList();
  }

  @Transactional
  public AchievementDtos.AttachmentResponse addAttachment(String achievementNo, AchievementDtos.AttachmentRequest req) {
    CurrentUser user = authContext.currentUser();
    int nextVersion = attachmentVersionRepository.findByBizTypeAndBizIdOrderByVersionAsc("ACHIEVEMENT", achievementNo).stream()
        .map(AttachmentVersionEntity::getVersion)
        .max(Integer::compareTo)
        .orElse(0) + 1;
    AttachmentVersionEntity entity = new AttachmentVersionEntity();
    entity.setBizType("ACHIEVEMENT");
    entity.setBizId(achievementNo);
    entity.setFileName(req.fileName());
    entity.setFilePath(req.filePath());
    entity.setMimeType(req.mimeType());
    entity.setSizeBytes(req.sizeBytes());
    entity.setFingerprintSha256(req.fingerprintSha256());
    entity.setVersion(nextVersion);
    entity.setUploadedBy(user.id());
    AttachmentVersionEntity saved = attachmentVersionRepository.save(entity);
    return new AchievementDtos.AttachmentResponse(
        saved.getId(), saved.getBizType(), saved.getBizId(), saved.getFileName(), saved.getFilePath(), saved.getMimeType(), saved.getSizeBytes(), saved.getFingerprintSha256(), saved.getVersion());
  }

  @Transactional(readOnly = true)
  public AchievementDtos.ExportTemplateResponse exportTemplate() {
    authContext.requireAnyRole("ADMIN", "MERCHANT", "REVIEWER");
    return new AchievementDtos.ExportTemplateResponse("/exports/templates/achievement_template.xlsx");
  }

  private AchievementDtos.AchievementResponse toResp(AchievementArchiveEntity a) {
    return new AchievementDtos.AchievementResponse(
        a.getId(), a.getAchievementNo(), a.getUserId(), a.getAchievementType(), a.getTitle(), a.getScore(), a.getVersion(), a.getStatus(), a.getCreatedAt());
  }
}
