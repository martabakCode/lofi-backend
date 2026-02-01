package com.lofi.lofiapps.service.impl.calculator;

import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.repository.LoanRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlafondCalculator {

  private final LoanRepository loanRepository;

  /**
   * Calculates the available plafond (remaining credit limit) for a user. Formula: Product
   * maxLoanAmount - Sum of all APPROVED/DISBURSED/COMPLETED loans
   */
  public BigDecimal calculateAvailablePlafond(User user, Product product) {
    if (product == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal maxPlafond = product.getMaxLoanAmount();

    // Calculate total of active/approved loans
    BigDecimal usedPlafond =
        loanRepository.findByCustomerId(user.getId()).stream()
            .filter(
                loan ->
                    loan.getLoanStatus() == LoanStatus.APPROVED
                        || loan.getLoanStatus() == LoanStatus.DISBURSED
                        || loan.getLoanStatus() == LoanStatus.COMPLETED)
            .map(loan -> loan.getLoanAmount() != null ? loan.getLoanAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal availablePlafond = maxPlafond.subtract(usedPlafond);

    // Ensure not negative
    return availablePlafond.compareTo(BigDecimal.ZERO) > 0 ? availablePlafond : BigDecimal.ZERO;
  }

  public BigDecimal calculateAvailablePlafond(User user) {
    if (user.getProduct() == null) {
      return BigDecimal.ZERO;
    }
    return calculateAvailablePlafond(user, user.getProduct());
  }
}
