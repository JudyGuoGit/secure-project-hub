package com.judy.secureprojecthub.controller.graphql;

import com.judy.secureprojecthub.entity.User;
import com.judy.secureprojecthub.graphql.payload.CreateUserInput;
import com.judy.secureprojecthub.graphql.payload.UpdateUserInput;
import com.judy.secureprojecthub.repository.UserRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class UserGraphQLController {

    private final UserRepository userRepository;

    public UserGraphQLController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<User> users() {
        return userRepository.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public User user(@Argument Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(@Argument CreateUserInput input) {
        User user = new User();
        user.setUsername(input.username());
        user.setEmail(input.email());
        user.setFullName(input.fullName());
        user.setBio(input.bio());
        user.setEnabled(input.enabled() == null || input.enabled());

        // TODO: Hash input.password() before saving.
        // Example: user.setPasswordHash(passwordEncoder.encode(input.password()));
        user.setPasswordHash(input.password());

        return userRepository.save(user);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(@Argument Long id, @Argument UpdateUserInput input) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        if (input.email() != null) user.setEmail(input.email());
        if (input.fullName() != null) user.setFullName(input.fullName());
        if (input.bio() != null) user.setBio(input.bio());
        if (input.enabled() != null) user.setEnabled(input.enabled());

        return userRepository.save(user);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteUser(@Argument Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }
}
