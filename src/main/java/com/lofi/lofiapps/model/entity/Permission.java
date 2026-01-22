package com.lofi.lofiapps.model.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends BaseDomainEntity {
  private String name;
  private String description;
}
