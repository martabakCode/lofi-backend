package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Data
public class MarketingApplyLoanRequest {
  // User basic data
  @io.swagger.v3.oas.annotations.media.Schema(
      description = "User's full name",
      example = "John Doe")
  @NotBlank
  private String fullName;

  @io.swagger.v3.oas.annotations.media.Schema(
      description = "User's email",
      example = "john.doe@example.com")
  @NotBlank
  @Email
  private String email;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Username", example = "johndoe")
  @NotBlank
  private String username;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Phone number", example = "08123456789")
  @NotBlank
  private String phoneNumber;

  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Branch ID",
      example = "00000000-0000-0000-0000-000000000001")
  @NotNull
  private UUID branchId;

  // Biodata fields
  @io.swagger.v3.oas.annotations.media.Schema(description = "Income source", example = "Salary")
  @NotBlank
  private String incomeSource;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Income type", example = "Monthly")
  @NotBlank
  private String incomeType;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Monthly income", example = "5000000")
  @NotNull
  private BigDecimal monthlyIncome;

  @io.swagger.v3.oas.annotations.media.Schema(
      description = "National Identity Number (NIK)",
      example = "1234567890123456")
  @NotBlank
  private String nik;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Date of birth", example = "1990-01-01")
  @NotNull
  private LocalDate dateOfBirth;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Place of birth", example = "Jakarta")
  @NotBlank
  private String placeOfBirth;

  @io.swagger.v3.oas.annotations.media.Schema(description = "City", example = "South Jakarta")
  @NotBlank
  private String city;

  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Address",
      example = "Jl. Sudirman No. 1")
  @NotBlank
  private String address;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Province", example = "DKI Jakarta")
  @NotBlank
  private String province;

  @io.swagger.v3.oas.annotations.media.Schema(description = "District", example = "Kebayoran Baru")
  @NotBlank
  private String district;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Sub-district", example = "Senayan")
  @NotBlank
  private String subDistrict;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Postal code", example = "12190")
  @NotBlank
  private String postalCode;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Gender", example = "MALE")
  @NotNull
  private com.lofi.lofiapps.enums.Gender gender;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Marital status", example = "SINGLE")
  @NotNull
  private com.lofi.lofiapps.enums.MaritalStatus maritalStatus;

  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Occupation",
      example = "Software Engineer")
  @NotBlank
  private String occupation;

  // Loan fields
  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Product ID",
      example = "00000000-0000-0000-0000-000000000002")
  @NotNull
  private UUID productId;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Loan amount", example = "10000000")
  @NotNull
  private BigDecimal loanAmount;

  @io.swagger.v3.oas.annotations.media.Schema(description = "Loan tenor in months", example = "12")
  @NotNull
  private Integer tenor;

  // Loan Purpose
  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Purpose of the loan",
      example = "Business expansion")
  private String purpose;

  // Bank Account Information for Disbursement
  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Bank name",
      example = "Bank Central Asia")
  private String bankName;

  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Bank branch",
      example = "Jakarta Main Branch")
  private String bankBranch;

  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Account number",
      example = "1234567890")
  private String accountNumber;

  @io.swagger.v3.oas.annotations.media.Schema(
      description = "Account holder name",
      example = "John Doe")
  private String accountHolderName;

  /**
   * Optional PIN for customer validation. If provided, PIN will be validated before loan
   * submission.
   */
  @jakarta.validation.constraints.Pattern(regexp = "^\\d{4,6}$", message = "PIN must be 4-6 digits")
  private String pin;
}
