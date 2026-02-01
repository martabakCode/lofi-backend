package com.lofi.lofiapps.entity;

import com.lofi.lofiapps.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id")
  private Branch branch;

  @NotBlank(message = "Username is required")
  @Column(unique = true, nullable = false)
  private String username;

  @NotBlank(message = "Full name is required")
  @Column(nullable = false)
  private String fullName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = true)
  private String password;

  @Column(nullable = true)
  private String phoneNumber;

  @Column(name = "profile_picture_url", nullable = true)
  private String profilePictureUrl;

  @Column(nullable = true)
  private String firebaseToken;

  @Column(name = "firebase_uid", nullable = true, unique = true)
  private String firebaseUid;

  @Enumerated(EnumType.STRING)
  private UserStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @Column(nullable = true)
  private int loansCompleted;

  @Column(nullable = true)
  private int totalOverdueDays;

  @Column(nullable = true)
  @Builder.Default
  private Boolean profileCompleted = false;

  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private UserBiodata userBiodata;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  // Location coordinates (nullable)
  @Column(precision = 10, scale = 8)
  private java.math.BigDecimal longitude;

  @Column(precision = 10, scale = 8)
  private java.math.BigDecimal latitude;
}
