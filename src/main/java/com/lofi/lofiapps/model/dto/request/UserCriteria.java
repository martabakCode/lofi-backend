package com.lofi.lofiapps.model.dto.request;

import com.lofi.lofiapps.model.enums.RoleName;
import com.lofi.lofiapps.model.enums.UserStatus;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCriteria {
  private UserStatus status;
  private RoleName roleName;
  private UUID branchId;
}
