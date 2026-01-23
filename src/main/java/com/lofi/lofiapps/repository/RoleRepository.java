package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.enums.RoleName;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
  Optional<Role> findByName(RoleName name);
}
