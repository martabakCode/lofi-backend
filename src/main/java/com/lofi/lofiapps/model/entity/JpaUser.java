package com.lofi.lofiapps.model.entity;

import com.lofi.lofiapps.model.enums.UserStatus;
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
public class JpaUser extends JpaBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id")
  private JpaBranch branch;

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

  @NotBlank(message = "Password is required")
  @Column(nullable = false)
  private String password;

  @Column(nullable = true)
  private String phoneNumber;

  @Column(name = "profile_picture_url", nullable = true)
  private String profilePictureUrl;

  @Column(nullable = true)
  private String firebaseToken;

  @Enumerated(EnumType.STRING)
  private UserStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private JpaProduct product;

  @Column(nullable = true)
  private int loansCompleted;

  @Column(nullable = true)
  private int totalOverdueDays;

  @Column(nullable = true)
  @Builder.Default
  private Boolean profileCompleted = false;

  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private JpaUserBiodata userBiodata;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<JpaRole> roles = new HashSet<>();
}
