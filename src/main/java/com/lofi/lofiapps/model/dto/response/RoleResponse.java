package com.lofi.lofiapps.model.dto.response;

import com.lofi.lofiapps.model.enums.RoleName;
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
