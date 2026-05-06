package com.judy.secureprojecthub.graphql.payload;

public record UpdateUserInput(
        String email,
        String fullName,
        String bio,
        Boolean enabled
) {}
