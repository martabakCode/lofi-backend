package com.lofi.lofiapps.model.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
  private UUID id;
  private String fullName;
  private String email;
  private String phoneNumber;
  private String profilePictureUrl;
  private BranchInfo branch;
  private BiodataInfo biodata;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BiodataInfo {
    private String incomeSource;
    private String incomeType;
    private java.math.BigDecimal monthlyIncome;
    private Integer age;
    private String nik;
    private java.time.LocalDate dateOfBirth;
    private String placeOfBirth;
    private String city;
    private String address;
    private String province;
    private String district;
    private String subDistrict;
    private String postalCode;
    private com.lofi.lofiapps.model.enums.Gender gender;
    private com.lofi.lofiapps.model.enums.MaritalStatus maritalStatus;
    private String education;
    private String occupation;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BranchInfo {
    private UUID id;
    private String name;
  }
}
