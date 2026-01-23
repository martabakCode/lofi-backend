package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.dto.request.CreateRoleRequest;
import com.lofi.lofiapps.dto.response.PermissionResponse;
import com.lofi.lofiapps.dto.response.RoleResponse;
import com.lofi.lofiapps.entity.Permission;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.repository.PermissionRepository;
import com.lofi.lofiapps.repository.RoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateRoleUseCase {
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  @Transactional
  public RoleResponse execute(CreateRoleRequest request) {
    if (roleRepository.findByName(request.getName()).isPresent()) {
      throw new IllegalStateException("Role already exists: " + request.getName());
    }

    Role role =
        Role.builder().name(request.getName()).description(request.getDescription()).build();

    // Set permissions if provided
    if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
      List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
      role.setPermissions(new HashSet<>(permissions));
    }

    Role saved = roleRepository.save(role);

    // Build permissions response
    List<PermissionResponse> permissionResponses =
        saved.getPermissions() != null
            ? saved.getPermissions().stream()
                .map(
                    p ->
                        PermissionResponse.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .description(p.getDescription())
                            .build())
                .collect(Collectors.toList())
            : List.of();

    return RoleResponse.builder()
        .id(saved.getId())
        .name(saved.getName())
        .description(saved.getDescription())
        .permissions(permissionResponses)
        .build();
  }
}
