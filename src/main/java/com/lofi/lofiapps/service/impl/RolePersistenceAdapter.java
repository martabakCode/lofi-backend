package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.mapper.RoleMapper;
import com.lofi.lofiapps.model.entity.Role;
import com.lofi.lofiapps.model.enums.RoleName;
import com.lofi.lofiapps.repository.JpaRoleRepository;
import com.lofi.lofiapps.repository.RoleRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RolePersistenceAdapter implements RoleRepository {
  private final JpaRoleRepository jpaRoleRepository;
  private final RoleMapper roleMapper;

  @Override
  public Optional<Role> findByName(RoleName name) {
    return jpaRoleRepository.findByName(name).map(roleMapper::toDomain);
  }
}
