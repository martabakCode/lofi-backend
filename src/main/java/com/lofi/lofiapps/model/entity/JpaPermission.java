package com.lofi.lofiapps.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE permissions SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class JpaPermission extends JpaBaseEntity {
  @Column(length = 50, unique = true, nullable = false)
  private String name;

  private String description;
}
