package com.judy.secureprojecthub.graphql.payload;

public record CreateAuditLogInput(
        Long userId,
        String action,
        String actionType,
        String description,
        String status,
        String ipAddress,
        String userAgent,
        String details
) {}
