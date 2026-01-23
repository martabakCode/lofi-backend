package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.dto.response.PermissionResponse;
import com.lofi.lofiapps.dto.response.RoleResponse;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.repository.RoleRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetRolesUseCase {
  private final RoleRepository roleRepository;

  public List<RoleResponse> execute() {
    return roleRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  private RoleResponse mapToResponse(Role role) {
    return RoleResponse.builder()
        .id(role.getId())
        .name(role.getName())
        .description(role.getDescription())
        .permissions(
            role.getPermissions() == null
                ? List.of()
                : role.getPermissions().stream()
                    .map(
                        p ->
                            PermissionResponse.builder()
                                .id(p.getId())
                                .name(p.getName())
                                .description(p.getDescription())
                                .build())
                    .collect(Collectors.toList()))
        .build();
  }
}
