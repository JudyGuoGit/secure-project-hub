package com.judy.secureprojecthub.repository;

import com.judy.secureprojecthub.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
}