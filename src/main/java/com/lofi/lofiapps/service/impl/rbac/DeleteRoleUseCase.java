package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.RoleRepository;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteRoleUseCase {
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;

  @Transactional
  public void execute(UUID roleId) {
    if (!roleRepository.existsById(roleId)) {
      throw new ResourceNotFoundException("Role", "id", roleId.toString());
    }

    // Check if role is assigned to any user
    if (userRepository.existsByRolesId(roleId)) {
      throw new IllegalStateException("Cannot delete role that is assigned to users");
    }

    roleRepository.deleteById(roleId);
  }
}
