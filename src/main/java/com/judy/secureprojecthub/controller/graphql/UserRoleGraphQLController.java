package com.judy.secureprojecthub.controller.graphql;

import com.judy.secureprojecthub.entity.Role;
import com.judy.secureprojecthub.entity.User;
import com.judy.secureprojecthub.entity.UserRole;
import com.judy.secureprojecthub.graphql.payload.CreateUserRoleInput;
import com.judy.secureprojecthub.graphql.payload.UpdateUserRoleInput;
import com.judy.secureprojecthub.repository.RoleRepository;
import com.judy.secureprojecthub.repository.UserRepository;
import com.judy.secureprojecthub.repository.UserRoleRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class UserRoleGraphQLController {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserRoleGraphQLController(
            UserRoleRepository userRoleRepository,
            UserRepository userRepository,
            RoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<UserRole> userRoles() {
        return userRoleRepository.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public UserRole userRole(@Argument Long id) {
        return userRoleRepository.findById(id).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserRole createUserRole(@Argument CreateUserRoleInput input) {
        return assignRoleToUser(input.userId(), input.roleId(), input.reason());
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserRole assignRoleToUser(@Argument Long userId, @Argument Long roleId, @Argument String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setReason(reason);
        userRole.setAssignedAt(LocalDateTime.now());
        return userRoleRepository.save(userRole);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserRole updateUserRole(@Argument Long id, @Argument UpdateUserRoleInput input) {
        UserRole userRole = userRoleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("UserRole not found: " + id));

        if (input.userId() != null) {
            userRole.setUser(userRepository.findById(input.userId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + input.userId())));
        }
        if (input.roleId() != null) {
            userRole.setRole(roleRepository.findById(input.roleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + input.roleId())));
        }
        if (input.reason() != null) userRole.setReason(input.reason());

        return userRoleRepository.save(userRole);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteUserRole(@Argument Long id) {
        if (!userRoleRepository.existsById(id)) {
            return false;
        }
        userRoleRepository.deleteById(id);
        return true;
    }
}
