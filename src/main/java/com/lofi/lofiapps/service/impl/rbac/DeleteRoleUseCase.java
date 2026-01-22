package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.JpaRoleRepository;
import com.lofi.lofiapps.repository.JpaUserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteRoleUseCase {
  private final JpaRoleRepository roleRepository;
  private final JpaUserRepository userRepository;

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
