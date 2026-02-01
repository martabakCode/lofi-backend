package com.lofi.lofiapps.dto.request;

import com.lofi.lofiapps.enums.Gender;
import com.lofi.lofiapps.enums.MaritalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdateProfileRequest {
  @Schema(description = "User's full name", example = "John Doe")
  @NotBlank
  private String fullName;

  @Schema(description = "User's phone number", example = "08123456789")
  @NotBlank
  private String phoneNumber;

  @Schema(description = "URL of profile picture", example = "https://example.com/photo.jpg")
  private String profilePictureUrl;

  // Biodata fields
  @Schema(description = "Source of income", example = "Salary")
  private String incomeSource;

  @Schema(description = "Type of income", example = "Monthly")
  private String incomeType;

  @Schema(description = "Monthly income amount", example = "5000000")
  private BigDecimal monthlyIncome;

  @Schema(description = "National Identity Number (NIK)", example = "1234567890123456")
  private String nik;

  @Schema(description = "Date of birth", example = "1990-01-01")
  private LocalDate dateOfBirth;

  @Schema(description = "Place of birth", example = "Jakarta")
  private String placeOfBirth;

  @Schema(description = "City of residence", example = "South Jakarta")
  private String city;

  @Schema(description = "Street address", example = "Jl. Sudirman No. 1")
  private String address;

  @Schema(description = "Province", example = "DKI Jakarta")
  private String province;

  @Schema(description = "District", example = "Kebayoran Baru")
  private String district;

  @Schema(description = "Sub-district", example = "Senayan")
  private String subDistrict;

  @Schema(description = "Postal code", example = "12190")
  private String postalCode;

  @Schema(description = "Gender", example = "MALE")
  private Gender gender;

  @Schema(description = "Marital status", example = "SINGLE")
  private MaritalStatus maritalStatus;

  @Schema(description = "Occupation", example = "Software Engineer")
  private String occupation;

  @Schema(description = "Longitude coordinate", example = "106.827183")
  private java.math.BigDecimal longitude;

  @Schema(description = "Latitude coordinate", example = "-6.175394")
  private java.math.BigDecimal latitude;
}
