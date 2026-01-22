package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.model.entity.JpaRole;
import com.lofi.lofiapps.model.entity.JpaUser;
import com.lofi.lofiapps.repository.JpaRoleRepository;
import com.lofi.lofiapps.repository.JpaUserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveRoleFromUserUseCase {
  private final JpaUserRepository userRepository;
  private final JpaRoleRepository roleRepository;

  @Transactional
  public void execute(UUID userId, UUID roleId) {
    JpaUser user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

    JpaRole role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId.toString()));

    if (user.getRoles() != null) {
      user.getRoles().remove(role);
      userRepository.save(user);
    }
  }
}
