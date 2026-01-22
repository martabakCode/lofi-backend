package com.lofi.lofiapps.service.impl.loan;

import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.model.dto.response.LoanResponse;
import com.lofi.lofiapps.model.entity.Loan;
import com.lofi.lofiapps.repository.LoanRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetLoanDetailUseCase {
  private final LoanRepository loanRepository;
  private final LoanDtoMapper loanDtoMapper;
  private final com.lofi.lofiapps.repository.JpaDocumentRepository documentRepository;

  @Transactional(readOnly = true)
  public LoanResponse execute(UUID loanId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
    LoanResponse response = loanDtoMapper.toResponse(loan);

    // Fetch documents
    java.util.List<com.lofi.lofiapps.model.dto.response.DocumentResponse> documents =
        documentRepository.findByLoanId(loanId).stream()
            .map(
                doc ->
                    com.lofi.lofiapps.model.dto.response.DocumentResponse.builder()
                        .id(doc.getId())
                        .fileName(doc.getFileName())
                        .documentType(doc.getDocumentType())
                        .uploadedAt(doc.getCreatedAt())
                        .build())
            .collect(java.util.stream.Collectors.toList());

    response.setDocuments(documents);
    return response;
  }
}
