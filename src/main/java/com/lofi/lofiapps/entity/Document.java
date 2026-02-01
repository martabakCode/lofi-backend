package com.lofi.lofiapps.entity;

import com.lofi.lofiapps.enums.DocumentType;
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
public class Document extends BaseEntity {

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

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "verified")
  @Builder.Default
  private Boolean verified = false;

  @Column(name = "verification_notes", columnDefinition = "TEXT")
  private String verificationNotes;
}
