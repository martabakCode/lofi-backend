package com.lofi.lofiapps.model.entity;

import com.lofi.lofiapps.model.enums.Gender;
import com.lofi.lofiapps.model.enums.MaritalStatus;
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
public class JpaUserBiodata extends JpaBaseEntity {
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private JpaUser user;

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

  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Enumerated(EnumType.STRING)
  private MaritalStatus maritalStatus;

  private String education;
  private String occupation;
}
