package com.judy.secureprojecthub.graphql.payload;

public record CreateUserRoleInput(Long userId, Long roleId, String reason) {}
