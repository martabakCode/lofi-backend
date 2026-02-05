package com.lofi.lofiapps.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "branches")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE branches SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Branch extends BaseEntity {
  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private String city;

  @Column(nullable = false)
  private String state;

  @Column(nullable = false)
  private String zipCode;

  @Column(nullable = false)
  private String phone;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isHeadOffice = false;

  @Column(precision = 11, scale = 8)
  private java.math.BigDecimal longitude;

  @Column(precision = 11, scale = 8)
  private java.math.BigDecimal latitude;
}
