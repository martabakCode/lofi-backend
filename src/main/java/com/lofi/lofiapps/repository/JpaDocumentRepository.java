package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.JpaDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDocumentRepository extends JpaRepository<JpaDocument, UUID> {
  List<JpaDocument> findByLoanId(UUID loanId);

  long countByLoanIdAndDocumentType(
      UUID loanId, com.lofi.lofiapps.model.enums.DocumentType documentType);
}
