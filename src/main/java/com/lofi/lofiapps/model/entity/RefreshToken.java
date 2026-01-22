package com.lofi.lofiapps.model.entity;

import java.time.Instant;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends BaseDomainEntity {
  private User user;
  private String token;
  private Instant expiryDate;
  private boolean revoked;
}
