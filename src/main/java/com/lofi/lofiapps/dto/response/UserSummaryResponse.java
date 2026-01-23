package com.lofi.lofiapps.dto.response;

import com.lofi.lofiapps.enums.UserStatus;
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
