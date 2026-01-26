package com.lofi.lofiapps.usecase.loan;

import com.lofi.lofiapps.dto.request.LoanCriteria;
import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.dto.response.ProductResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.repository.LoanRepository;
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

    public PagedResponse<LoanResponse> execute(LoanCriteria criteria, Pageable pageable) {
        Specification<Loan> spec = (root, query, cb) -> {
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
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Loan> page = loanRepository.findAll(spec, pageable);

        List<LoanResponse> items = page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

        return PagedResponse.of(
                items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private LoanResponse mapToResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .customerId(loan.getCustomer() != null ? loan.getCustomer().getId() : null)
                .customerName(loan.getCustomer() != null ? loan.getCustomer().getFullName() : null)
                .product(
                        loan.getProduct() != null
                                ? ProductResponse.builder()
                                        .id(loan.getProduct().getId())
                                        .productCode(loan.getProduct().getProductCode())
                                        .productName(loan.getProduct().getProductName())
                                        .interestRate(loan.getProduct().getInterestRate())
                                        .build()
                                : null)
                .loanAmount(loan.getLoanAmount())
                .tenor(loan.getTenor())
                .loanStatus(loan.getLoanStatus())
                .currentStage(loan.getCurrentStage())
                .submittedAt(loan.getSubmittedAt())
                .approvedAt(loan.getApprovedAt())
                .rejectedAt(loan.getRejectedAt())
                .build();
    }
}
