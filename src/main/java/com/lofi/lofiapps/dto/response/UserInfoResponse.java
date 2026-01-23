package com.lofi.lofiapps.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
  private UUID id;
  private String email;
  private String username;
  private UUID branchId;
  private String branchName;
  private List<String> roles;
  private java.math.BigDecimal plafond;
  private List<String> permissions;
}
