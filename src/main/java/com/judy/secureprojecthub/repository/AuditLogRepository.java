package com.judy.secureprojecthub.repository;

import com.judy.secureprojecthub.entity.AuditLog;
import com.judy.secureprojecthub.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
 

	List<AuditLog> findByUserId(Long userId);
}
