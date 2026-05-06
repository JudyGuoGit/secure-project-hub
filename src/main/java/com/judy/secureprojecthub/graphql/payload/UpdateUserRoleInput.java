package com.judy.secureprojecthub.graphql.payload;

public record UpdateUserRoleInput(Long userId, Long roleId, String reason) {}
