package com.judy.secureprojecthub.graphql.payload;

public record CreateUserInput(
        String username,
        String email,
        String password,
        String fullName,
        String bio,
        Boolean enabled
) {}
