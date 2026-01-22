package com.lofi.lofiapps.model.entity;

import com.lofi.lofiapps.model.enums.RoleName;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseDomainEntity {
  private RoleName name;
  private Set<Permission> permissions;
}
