package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.request.LoanCriteria;
import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.repository.DocumentRepository;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.service.impl.mapper.DocumentMapper;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetLoansUseCase {

  private final LoanRepository loanRepository;
  private final LoanDtoMapper loanDtoMapper;
  private final DocumentRepository documentRepository;
  private final AnalyzeLoanUseCase analyzeLoanUseCase;
  private final DocumentMapper documentMapper;

  public PagedResponse<LoanResponse> execute(LoanCriteria criteria, Pageable pageable) {
    Specification<Loan> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (criteria.getStatus() != null) {
            predicates.add(cb.equal(root.get("loanStatus"), criteria.getStatus()));
          }
          if (criteria.getCustomerId() != null) {
            predicates.add(cb.equal(root.get("customer").get("id"), criteria.getCustomerId()));
          }
          if (criteria.getBranchId() != null) {
            predicates.add(cb.equal(root.get("branch").get("id"), criteria.getBranchId()));
          }
          // Exclude specific statuses (e.g., DRAFT and CANCELLED for active loans)
          if (criteria.getExcludeStatuses() != null && !criteria.getExcludeStatuses().isEmpty()) {
            predicates.add(root.get("loanStatus").in(criteria.getExcludeStatuses()).not());
          }
          return cb.and(predicates.toArray(new Predicate[0]));
        };

    Page<Loan> page = loanRepository.findAll(spec, pageable);

    List<LoanResponse> items =
        page.getContent().stream()
            .map(
                loan -> {
                  LoanResponse resp = loanDtoMapper.toResponse(loan);
                  // Populate documents
                  resp.setDocuments(
                      documentRepository.findByLoanId(loan.getId()).stream()
                          .map(documentMapper::toResponse)
                          .collect(Collectors.toList()));
                  // Populate AI Analysis
                  resp.setAiAnalysis(analyzeLoanUseCase.execute(loan.getId()));
                  return resp;
                })
            .collect(Collectors.toList());

    return PagedResponse.of(
        items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }
}
