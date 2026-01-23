package com.lofi.lofiapps.dto.request;

import com.lofi.lofiapps.enums.RoleName;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateRoleRequest {
  @NotNull(message = "Role name is required")
  private RoleName name;

  private String description;

  private List<UUID> permissionIds;
}
