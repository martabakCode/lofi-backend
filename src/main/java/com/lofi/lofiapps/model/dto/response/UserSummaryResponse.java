package com.lofi.lofiapps.model.dto.response;

import com.lofi.lofiapps.model.enums.UserStatus;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryResponse {
  private UUID id;
  private String fullName;
  private String username;
  private String email;
  private Set<String> roles;
  private UserStatus status;
  private String branchName;
}
