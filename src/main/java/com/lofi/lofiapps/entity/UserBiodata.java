package com.lofi.lofiapps.entity;

import com.lofi.lofiapps.enums.Gender;
import com.lofi.lofiapps.enums.JobType;
import com.lofi.lofiapps.enums.MaritalStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "user_biodatas")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE user_biodatas SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserBiodata extends BaseEntity {
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

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

  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Enumerated(EnumType.STRING)
  private MaritalStatus maritalStatus;

  private String occupation;

  // Employment/Business Details
  @Enumerated(EnumType.STRING)
  private JobType jobType; // KARYAWAN, WIRASWASTA, PROFESIONAL

  private String companyName;
  private String jobPosition;
  private Integer workDurationMonths;
  private String workAddress;
  private String officePhoneNumber;
  private BigDecimal additionalIncome;

  // Emergency Contact
  private String emergencyContactName;
  private String emergencyContactRelation;
  private String emergencyContactPhone;
  private String emergencyContactAddress;

  // NPWP
  private String npwpNumber;

  // Age validation fields
  private Integer ageAtLoanApplication;
  private Integer ageAtLoanCompletion;
}
