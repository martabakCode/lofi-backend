package com.lofi.lofiapps.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JpaNotification extends JpaBaseEntity {

  @NotNull
  @Column(nullable = false)
  private UUID userId;

  @NotBlank
  @Column(nullable = false)
  private String title;

  @NotBlank
  @Column(nullable = false, columnDefinition = "TEXT")
  private String message;

  private String type;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isRead = false;

  private String link;
}
