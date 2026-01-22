package com.lofi.lofiapps.service.impl.report;

import com.lofi.lofiapps.model.dto.response.LoanKpiResponse;
import com.lofi.lofiapps.model.entity.Loan;
import com.lofi.lofiapps.model.enums.LoanStatus;
import com.lofi.lofiapps.repository.LoanRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetLoanKpisUseCase {
  private final LoanRepository loanRepository;

  public LoanKpiResponse execute() {
    List<Loan> allLoans = loanRepository.findAll();

    Map<String, Long> loansByProduct =
        allLoans.stream()
            .collect(
                Collectors.groupingBy(l -> l.getProduct().getProductName(), Collectors.counting()));

    return LoanKpiResponse.builder()
        .totalLoans(allLoans.size())
        .totalSubmitted(loanRepository.countByStatus(LoanStatus.SUBMITTED))
        .totalReviewed(loanRepository.countByStatus(LoanStatus.REVIEWED))
        .totalApproved(loanRepository.countByStatus(LoanStatus.APPROVED))
        .totalRejected(loanRepository.countByStatus(LoanStatus.REJECTED))
        .totalCancelled(loanRepository.countByStatus(LoanStatus.CANCELLED))
        .totalDisbursed(loanRepository.countByStatus(LoanStatus.DISBURSED))
        .totalCompleted(loanRepository.countByStatus(LoanStatus.COMPLETED))
        .loansByProduct(loansByProduct)
        .build();
  }
}
