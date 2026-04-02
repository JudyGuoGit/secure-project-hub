package com.judy.secureprojecthub.repository;

import com.judy.secureprojecthub.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
