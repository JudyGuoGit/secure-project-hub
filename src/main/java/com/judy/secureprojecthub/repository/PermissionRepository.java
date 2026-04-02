package com.judy.secureprojecthub.repository;

import com.judy.secureprojecthub.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
