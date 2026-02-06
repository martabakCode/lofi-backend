package com.lofi.lofiapps.service.impl.usecase.rbac;

import com.lofi.lofiapps.dto.request.AssignRolesRequest;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.RoleRepository;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignRolesToUserUseCase {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Transactional
  public void execute(UUID userId, AssignRolesRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));

    // Enforce rule: Customer roles cannot be changed manually through this API
    // easily
    // Usually, we check if the target user is an internal user
    if (user.getRoles() == null) {
      user.setRoles(new HashSet<>());
    }

    boolean isCustomer =
        user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_CUSTOMER);
    if (isCustomer) {
      throw new IllegalStateException(
          "Customer roles cannot be managed through this administrative API");
    }

    List<Role> roles = roleRepository.findAllById(request.getRoleIds());
    if (roles.size() != request.getRoleIds().size()) {
      throw new ResourceNotFoundException("Role", "ids", request.getRoleIds().toString());
    }

    // Prevent assigning CUSTOMER role manually
    if (roles.stream().anyMatch(r -> r.getName() == RoleName.ROLE_CUSTOMER)) {
      throw new IllegalArgumentException("Cannot manually assign CUSTOMER role");
    }

    if (user.getRoles() == null) {
      user.setRoles(new HashSet<>());
    }

    user.getRoles().addAll(roles);
    userRepository.save(user);
  }
}
