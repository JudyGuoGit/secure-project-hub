package com.judy.secureprojecthub.controller.graphql;

import com.judy.secureprojecthub.entity.AuditLog;
import com.judy.secureprojecthub.entity.User;
import com.judy.secureprojecthub.graphql.payload.CreateAuditLogInput;
import com.judy.secureprojecthub.graphql.payload.UpdateAuditLogInput;
import com.judy.secureprojecthub.repository.AuditLogRepository;
import com.judy.secureprojecthub.repository.UserRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AuditLogGraphQLController {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogGraphQLController(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> auditLogs() {
        return auditLogRepository.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AuditLog auditLog(@Argument Long id) {
        return auditLogRepository.findById(id).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AuditLog createAuditLog(@Argument CreateAuditLogInput input) {
        AuditLog auditLog = new AuditLog();
        applyInput(auditLog, input.userId(), input.action(), input.actionType(), input.description(), input.status(), input.ipAddress(), input.userAgent(), input.details());
        if (auditLog.getCreatedAt() == null) auditLog.setCreatedAt(LocalDateTime.now());
        return auditLogRepository.save(auditLog);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AuditLog updateAuditLog(@Argument Long id, @Argument UpdateAuditLogInput input) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AuditLog not found: " + id));
        applyInput(auditLog, input.userId(), input.action(), input.actionType(), input.description(), input.status(), input.ipAddress(), input.userAgent(), input.details());
        return auditLogRepository.save(auditLog);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteAuditLog(@Argument Long id) {
        if (!auditLogRepository.existsById(id)) {
            return false;
        }
        auditLogRepository.deleteById(id);
        return true;
    }

    private void applyInput(AuditLog auditLog, Long userId, String action, String actionType, String description,
                            String status, String ipAddress, String userAgent, String details) {
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            auditLog.setUser(user);
        }
        if (action != null) auditLog.setAction(action);
        if (actionType != null) auditLog.setActionType(actionType);
        if (description != null) auditLog.setDescription(description);
        if (status != null) auditLog.setStatus(status);
        if (ipAddress != null) auditLog.setIpAddress(ipAddress);
        if (userAgent != null) auditLog.setUserAgent(userAgent);
        if (details != null) auditLog.setDetails(details);
    }
}
