package com.lofi.lofiapps.service.impl.calculator;

import com.lofi.lofiapps.entity.Product;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.repository.LoanRepository;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlafondCalculator {

  private final LoanRepository loanRepository;

  private static final Set<LoanStatus> PLAFOND_CONSUMING_STATUSES =
      EnumSet.of(
          LoanStatus.SUBMITTED,
          LoanStatus.REVIEWED,
          LoanStatus.APPROVED,
          LoanStatus.DISBURSED,
          LoanStatus.COMPLETED);

  /**
   * Calculates the available plafond (remaining credit limit) for a user. Formula: Product
   * maxLoanAmount - Sum of all SUBMITTED/REVIEWED/APPROVED/DISBURSED/COMPLETED loans
   */
  public BigDecimal calculateAvailablePlafond(User user, Product product) {
    return calculateAvailablePlafond(user, product, null);
  }

  /**
   * Same as {@link #calculateAvailablePlafond(User, Product)} but excludes a specific loan (useful
   * when validating transitions for an existing loan).
   */
  public BigDecimal calculateAvailablePlafond(User user, Product product, UUID excludeLoanId) {
    if (product == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal maxPlafond = product.getMaxLoanAmount();

    // Calculate total of active/approved loans
    BigDecimal usedPlafond =
        loanRepository.findByCustomerId(user.getId()).stream()
            .filter(loan -> excludeLoanId == null || !excludeLoanId.equals(loan.getId()))
            .filter(loan -> PLAFOND_CONSUMING_STATUSES.contains(loan.getLoanStatus()))
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

  public BigDecimal calculateAvailablePlafond(User user, UUID excludeLoanId) {
    if (user.getProduct() == null) {
      return BigDecimal.ZERO;
    }
    return calculateAvailablePlafond(user, user.getProduct(), excludeLoanId);
  }
}
