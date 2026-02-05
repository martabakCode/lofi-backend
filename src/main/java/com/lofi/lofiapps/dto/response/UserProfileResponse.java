package com.lofi.lofiapps.dto.response;

import com.lofi.lofiapps.enums.Gender;
import com.lofi.lofiapps.enums.MaritalStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
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
  private ProductResponse product;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BiodataInfo {
    private String incomeSource;
    private String incomeType;
    private BigDecimal monthlyIncome;
    private String nik;
    private LocalDate dateOfBirth;
    private String placeOfBirth;
    private String city;
    private String address;
    private String province;
    private String district;
    private String subDistrict;
    private String postalCode;
    private Gender gender;
    private MaritalStatus maritalStatus;

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

  /** Available plafond (remaining credit limit) after deducting approved/active loans */
  private BigDecimal availablePlafond;

  private BigDecimal totalApprovedLoans;
  private Boolean hasActiveLoan;
  private Boolean pinSet;
  private Boolean profileCompleted;
}
