package com.lofi.lofiapps.service.impl.usecase.user;

import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.LoanService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteUserUseCase {

  private final UserRepository userRepository;
  private final LoanRepository loanRepository;
  private final LoanService loanService;

  @Transactional
  public void execute(UUID userId) {
    // 1. Find all active loans for this user
    List<Loan> userLoans = loanRepository.findByCustomerId(userId);

    // 2. Cancel all active loans
    for (Loan loan : userLoans) {
      LoanStatus status = loan.getLoanStatus();
      if (status == LoanStatus.DRAFT
          || status == LoanStatus.SUBMITTED
          || status == LoanStatus.REVIEWED
          || status == LoanStatus.APPROVED) {
        loanService.rejectLoan(loan.getId(), "SYSTEM", "User account deleted");
      }
    }

    // 3. Delete the user
    userRepository.deleteById(userId);
  }
}
