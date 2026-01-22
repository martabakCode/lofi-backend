package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.JpaUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository
    extends JpaRepository<JpaUser, UUID>, JpaSpecificationExecutor<JpaUser> {
  Optional<JpaUser> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByRolesId(UUID roleId);
}
