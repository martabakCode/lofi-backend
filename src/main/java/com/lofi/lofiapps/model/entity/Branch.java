package com.lofi.lofiapps.model.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Branch extends BaseDomainEntity {
  private String name;
  private String address;
  private String city;
  private String state;
  private String zipCode;
  private String phone;
  private String longitude;
  private String latitude;
}
