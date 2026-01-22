package com.lofi.lofiapps.model.entity;

import com.lofi.lofiapps.model.enums.Gender;
import com.lofi.lofiapps.model.enums.MaritalStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserBiodata extends BaseDomainEntity {
  private String incomeSource;
  private String incomeType;
  private BigDecimal monthlyIncome;
  private int age;
  private String nik;
  private LocalDate dateOfBirth;
  private String placeOfBirth;
  private String city;
  private String address;
  private String province;
  private String district;
  private String subDistrict;
  private String postalCode;
  private String phoneNumber;
  private Gender gender;
  private MaritalStatus maritalStatus;
  private String education;
  private String occupation;
}
