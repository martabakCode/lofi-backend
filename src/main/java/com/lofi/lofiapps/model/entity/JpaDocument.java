package com.lofi.lofiapps.model.entity;

import com.lofi.lofiapps.model.enums.DocumentType;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "documents")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JpaDocument extends JpaBaseEntity {

  @Column(nullable = false)
  private UUID loanId;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String objectKey;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentType documentType;

  @Column(nullable = false)
  private UUID uploadedBy;
}
