package com.lofi.lofiapps.service.impl.usecase.rbac;

import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.RoleRepository;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveRoleFromUserUseCase {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Transactional
  public void execute(UUID userId, UUID roleId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId.toString()));

    if (user.getRoles() != null) {
      user.getRoles().remove(role);
      userRepository.save(user);
    }
  }
}
