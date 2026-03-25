package com.company.petplatform.reporting;

import com.company.petplatform.im.MessageRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SchedulerJobs {

  private final JobExecutionRepository jobExecutionRepository;
  private final BackupRecordRepository backupRecordRepository;
  private final MessageRepository messageRepository;

  @Value("${app.im.retention-days:180}")
  private int messageRetentionDays;

  @Value("${app.backup.retention-days:30}")
  private int backupRetentionDays;

  public SchedulerJobs(
      JobExecutionRepository jobExecutionRepository,
      BackupRecordRepository backupRecordRepository,
      MessageRepository messageRepository) {
    this.jobExecutionRepository = jobExecutionRepository;
    this.backupRecordRepository = backupRecordRepository;
    this.messageRepository = messageRepository;
  }

  @Scheduled(cron = "${app.reporting.cron:0 0 2 * * ?}")
  @Transactional
  public void dailyReportJob() {
    JobExecutionEntity job = new JobExecutionEntity();
    job.setJobType("REPORT");
    job.setTriggerType("SCHEDULED");
    job.setStatus("RUNNING");
    job.setStartedAt(LocalDateTime.now());
    JobExecutionEntity saved = jobExecutionRepository.save(job);
    saved.setStatus("SUCCESS");
    saved.setEndedAt(LocalDateTime.now());
    jobExecutionRepository.save(saved);
  }

  @Scheduled(cron = "${app.backup.full-cron:0 0 1 * * ?}")
  @Transactional
  public void fullBackupJob() {
    BackupRecordEntity rec = new BackupRecordEntity();
    rec.setBackupNo("BKP-FULL-" + System.currentTimeMillis());
    rec.setBackupType("FULL");
    rec.setStoragePath("/backup/full/" + System.currentTimeMillis() + ".sql");
    rec.setStatus("SUCCESS");
    rec.setStartedAt(LocalDateTime.now());
    rec.setCompletedAt(LocalDateTime.now());
    rec.setRetentionUntil(LocalDateTime.now().plusDays(backupRetentionDays));
    backupRecordRepository.save(rec);
  }

  @Scheduled(cron = "${app.backup.incremental-cron:0 0 * * * ?}")
  @Transactional
  public void incrementalBackupJob() {
    BackupRecordEntity rec = new BackupRecordEntity();
    rec.setBackupNo("BKP-INC-" + System.currentTimeMillis());
    rec.setBackupType("INCREMENTAL");
    rec.setStoragePath("/backup/incremental/" + System.currentTimeMillis() + ".bin");
    rec.setStatus("SUCCESS");
    rec.setStartedAt(LocalDateTime.now());
    rec.setCompletedAt(LocalDateTime.now());
    rec.setRetentionUntil(LocalDateTime.now().plusDays(backupRetentionDays));
    backupRecordRepository.save(rec);
  }

  @Scheduled(cron = "0 0 * * * ?")
  @Transactional
  public void retentionCleanupJob() {
    LocalDateTime expiry = LocalDateTime.now().minusDays(messageRetentionDays);
    messageRepository.deleteByExpiresAtBefore(expiry);

    JobExecutionEntity job = new JobExecutionEntity();
    job.setJobType("RETENTION");
    job.setTriggerType("SCHEDULED");
    job.setStatus("SUCCESS");
    job.setStartedAt(LocalDateTime.now());
    job.setEndedAt(LocalDateTime.now());
    jobExecutionRepository.save(job);
  }
}
