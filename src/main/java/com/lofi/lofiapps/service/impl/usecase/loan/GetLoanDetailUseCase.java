package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.DocumentResponse;
import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.DocumentRepository;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.service.impl.mapper.DocumentMapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetLoanDetailUseCase {

  private final LoanRepository loanRepository;
  private final DocumentRepository documentRepository;
  private final LoanDtoMapper loanDtoMapper;
  private final DocumentMapper documentMapper;

  @Transactional(readOnly = true)
  public LoanResponse execute(UUID loanId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
    LoanResponse response = loanDtoMapper.toResponse(loan);

    // Fetch documents
    List<DocumentResponse> documents =
        documentRepository.findByLoanId(loanId).stream()
            .map(documentMapper::toResponse)
            .collect(Collectors.toList());

    response.setDocuments(documents);
    return response;
  }
}
