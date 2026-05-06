package com.judy.secureprojecthub.graphql.payload;

public record CreatePermissionInput(
        String name,
        String description,
        String resource,
        String action
) {}
