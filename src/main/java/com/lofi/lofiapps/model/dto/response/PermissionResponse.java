package com.lofi.lofiapps.model.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponse {
  private UUID id;
  private String name;
  private String description;
}
