package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.entity.Permission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
  Optional<Permission> findByName(String name);
}
