package com.lofi.lofiapps.service.impl.report;

import com.lofi.lofiapps.dto.response.LoanKpiResponse;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.LoanStatus;
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
        .totalSubmitted(loanRepository.countByLoanStatus(LoanStatus.SUBMITTED))
        .totalReviewed(loanRepository.countByLoanStatus(LoanStatus.REVIEWED))
        .totalApproved(loanRepository.countByLoanStatus(LoanStatus.APPROVED))
        .totalRejected(loanRepository.countByLoanStatus(LoanStatus.REJECTED))
        .totalCancelled(loanRepository.countByLoanStatus(LoanStatus.CANCELLED))
        .totalDisbursed(loanRepository.countByLoanStatus(LoanStatus.DISBURSED))
        .totalCompleted(loanRepository.countByLoanStatus(LoanStatus.COMPLETED))
        .loansByProduct(loansByProduct)
        .build();
  }
}
