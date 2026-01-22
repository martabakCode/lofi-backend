package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.JpaRole;
import com.lofi.lofiapps.model.enums.RoleName;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRoleRepository extends JpaRepository<JpaRole, UUID> {
  Optional<JpaRole> findByName(RoleName name);
}
