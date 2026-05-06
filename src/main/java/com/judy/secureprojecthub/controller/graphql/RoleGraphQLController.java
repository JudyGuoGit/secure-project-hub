package com.judy.secureprojecthub.controller.graphql;

import com.judy.secureprojecthub.entity.Role;
import com.judy.secureprojecthub.graphql.payload.CreateRoleInput;
import com.judy.secureprojecthub.graphql.payload.UpdateRoleInput;
import com.judy.secureprojecthub.repository.RoleRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class RoleGraphQLController {

    private final RoleRepository roleRepository;

    public RoleGraphQLController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<Role> roles() {
        return roleRepository.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Role role(@Argument Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Role createRole(@Argument CreateRoleInput input) {
        Role role = new Role();
        role.setName(input.name());
        role.setDescription(input.description());
        return roleRepository.save(role);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Role updateRole(@Argument Long id, @Argument UpdateRoleInput input) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + id));

        if (input.name() != null) role.setName(input.name());
        if (input.description() != null) role.setDescription(input.description());

        return roleRepository.save(role);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteRole(@Argument Long id) {
        if (!roleRepository.existsById(id)) {
            return false;
        }
        roleRepository.deleteById(id);
        return true;
    }
}
