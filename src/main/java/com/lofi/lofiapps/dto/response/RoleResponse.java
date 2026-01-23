package com.lofi.lofiapps.dto.response;

import com.lofi.lofiapps.enums.RoleName;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {
  private UUID id;
  private RoleName name;
  private String description;
  private List<PermissionResponse> permissions;
}
