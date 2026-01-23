package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
  @NotBlank private String fullName;
  @NotBlank private String phoneNumber;
  private String profilePictureUrl;

  // Biodata fields
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
  private com.lofi.lofiapps.enums.Gender gender;
  private com.lofi.lofiapps.enums.MaritalStatus maritalStatus;
  private String education;
  private String occupation;
}
