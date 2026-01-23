package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.entity.Document;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
  List<Document> findByLoanId(UUID loanId);

  long countByLoanIdAndDocumentType(UUID loanId, com.lofi.lofiapps.enums.DocumentType documentType);
}
