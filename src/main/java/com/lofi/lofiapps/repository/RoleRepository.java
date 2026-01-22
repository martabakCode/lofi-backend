package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.Role;
import com.lofi.lofiapps.model.enums.RoleName;
import java.util.Optional;

public interface RoleRepository {
  Optional<Role> findByName(RoleName name);
}
