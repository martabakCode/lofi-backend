package com.lofi.lofiapps.model.entity;

import com.lofi.lofiapps.model.enums.UserStatus;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseDomainEntity {
  private Branch branch;
  private String username;
  private String fullName;
  private String email;
  private String password;
  private String phoneNumber;
  private String firebaseToken;
  private UserStatus status;
  private Product product;
  private int loansCompleted;
  private int totalOverdueDays;
  private String profilePictureUrl;
  @Builder.Default private Boolean profileCompleted = false;
  private UserBiodata userBiodata;
  @Builder.Default private Set<Role> roles = new HashSet<>();
}
