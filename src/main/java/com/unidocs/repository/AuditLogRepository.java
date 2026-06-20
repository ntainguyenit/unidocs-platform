package com.unidocs.repository;

import com.unidocs.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    void deleteByTimestampBefore(LocalDateTime date);
}
