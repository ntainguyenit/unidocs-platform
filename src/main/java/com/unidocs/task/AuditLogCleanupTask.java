package com.unidocs.task;

import com.unidocs.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AuditLogCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(AuditLogCleanupTask.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogCleanupTask(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Run every day at 3 AM
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldAuditLogs() {
        log.info("Starting scheduled cleanup of old audit logs...");
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        try {
            auditLogRepository.deleteByTimestampBefore(threeMonthsAgo);
            log.info("Successfully cleaned up audit logs older than {}", threeMonthsAgo);
        } catch (Exception e) {
            log.error("Failed to clean up old audit logs: {}", e.getMessage(), e);
        }
    }
}
