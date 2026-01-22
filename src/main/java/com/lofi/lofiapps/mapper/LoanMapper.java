package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.entity.JpaLoan;
import com.lofi.lofiapps.model.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanMapper {
  private final UserMapper userMapper;
  private final ProductMapper productMapper;
  private final BranchMapper branchMapper;

  public Loan toDomain(JpaLoan entity) {
    if (entity == null) return null;
    return Loan.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .lastModifiedBy(entity.getLastModifiedBy())
        .deletedAt(entity.getDeletedAt())
        .customer(userMapper.toDomain(entity.getCustomer()))
        .branch(branchMapper.toDomain(entity.getBranch()))
        .product(productMapper.toDomain(entity.getProduct()))
        .loanAmount(entity.getLoanAmount())
        .tenor(entity.getTenor())
        .loanStatus(entity.getLoanStatus())
        .currentStage(entity.getCurrentStage())
        .submittedAt(entity.getSubmittedAt())
        .approvedAt(entity.getApprovedAt())
        .rejectedAt(entity.getRejectedAt())
        .disbursedAt(entity.getDisbursedAt())
        .disbursementReference(entity.getDisbursementReference())
        .lastStatusChangedAt(entity.getLastStatusChangedAt())
        .build();
  }

  public JpaLoan toJpa(Loan domain) {
    if (domain == null) return null;
    return JpaLoan.builder()
        .id(domain.getId())
        .createdAt(domain.getCreatedAt())
        .updatedAt(domain.getUpdatedAt())
        .createdBy(domain.getCreatedBy())
        .lastModifiedBy(domain.getLastModifiedBy())
        .deletedAt(domain.getDeletedAt())
        .customer(userMapper.toJpa(domain.getCustomer()))
        .branch(branchMapper.toJpa(domain.getBranch()))
        .product(productMapper.toJpa(domain.getProduct()))
        .loanAmount(domain.getLoanAmount())
        .tenor(domain.getTenor())
        .loanStatus(domain.getLoanStatus())
        .currentStage(domain.getCurrentStage())
        .submittedAt(domain.getSubmittedAt())
        .approvedAt(domain.getApprovedAt())
        .rejectedAt(domain.getRejectedAt())
        .disbursedAt(domain.getDisbursedAt())
        .disbursementReference(domain.getDisbursementReference())
        .lastStatusChangedAt(domain.getLastStatusChangedAt())
        .build();
  }
}
