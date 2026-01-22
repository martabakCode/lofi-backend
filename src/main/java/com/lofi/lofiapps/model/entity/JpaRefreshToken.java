package com.lofi.lofiapps.model.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE refresh_tokens SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class JpaRefreshToken extends JpaBaseEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private JpaUser user;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private Instant expiryDate;

  @Column(nullable = false)
  private boolean revoked;
}
