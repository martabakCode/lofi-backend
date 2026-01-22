package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.JpaPermission;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPermissionRepository extends JpaRepository<JpaPermission, UUID> {
  Optional<JpaPermission> findByName(String name);
}
