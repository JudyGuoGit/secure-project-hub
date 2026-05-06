package com.judy.secureprojecthub.graphql.payload;

public record UpdatePermissionInput(
        String name,
        String description,
        String resource,
        String action
) {}
