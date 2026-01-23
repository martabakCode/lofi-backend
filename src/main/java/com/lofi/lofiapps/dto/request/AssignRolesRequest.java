package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class AssignRolesRequest {
  @NotEmpty(message = "Role IDs are required")
  private List<UUID> roleIds;
}
