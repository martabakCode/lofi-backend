package com.lofi.lofiapps.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class AssignPermissionsRequest {
  @NotEmpty(message = "Permission IDs are required")
  private List<UUID> permissionIds;
}
