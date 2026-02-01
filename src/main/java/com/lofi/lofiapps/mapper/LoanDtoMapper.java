package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.dto.response.LoanResponse;
import com.lofi.lofiapps.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanDtoMapper {
  private final ProductDtoMapper productDtoMapper;

  public LoanResponse toResponse(Loan domain) {
    if (domain == null) return null;
    return LoanResponse.builder()
        .id(domain.getId())
        .customerId(domain.getCustomer() != null ? domain.getCustomer().getId() : null)
        .customerName(domain.getCustomer() != null ? domain.getCustomer().getFullName() : null)
        .product(productDtoMapper.toResponse(domain.getProduct()))
        .loanAmount(domain.getLoanAmount())
        .tenor(domain.getTenor())
        .loanStatus(domain.getLoanStatus())
        .currentStage(domain.getCurrentStage())
        .submittedAt(domain.getSubmittedAt())
        .approvedAt(domain.getApprovedAt())
        .rejectedAt(domain.getRejectedAt())
        .disbursedAt(domain.getDisbursedAt())
        .disbursementReference(domain.getDisbursementReference())
        .longitude(domain.getLongitude())
        .latitude(domain.getLatitude())
        .declaredIncome(domain.getDeclaredIncome())
        .npwpNumber(domain.getNpwpNumber())
        .jobType(domain.getJobType())
        .companyName(domain.getCompanyName())
        .jobPosition(domain.getJobPosition())
        .workDurationMonths(domain.getWorkDurationMonths())
        .workAddress(domain.getWorkAddress())
        .officePhoneNumber(domain.getOfficePhoneNumber())
        .additionalIncome(domain.getAdditionalIncome())
        .emergencyContactName(domain.getEmergencyContactName())
        .emergencyContactRelation(domain.getEmergencyContactRelation())
        .emergencyContactPhone(domain.getEmergencyContactPhone())
        .emergencyContactAddress(domain.getEmergencyContactAddress())
        .downPayment(domain.getDownPayment())
        .purpose(domain.getPurpose())
        .bankName(domain.getBankName())
        .bankBranch(domain.getBankBranch())
        .accountNumber(domain.getAccountNumber())
        .accountHolderName(domain.getAccountHolderName())
        .interestRate(domain.getInterestRate())
        .adminFee(domain.getAdminFee())
        .build();
  }
}
