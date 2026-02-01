package com.lofi.lofiapps.service.impl.validator;

import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.entity.UserBiodata;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class RiskValidator {

  private static final int MAX_OVERDUE_DAYS = 30;
  private static final int MAX_OVERDUE_FOR_HIGH_LOAN_HISTORY = 10;
  private static final int MAX_COMPLETED_LOANS_FOR_RISK = 5;
  private static final double MAX_DEBT_TO_INCOME_RATIO = 10.0;
  private static final BigDecimal MIN_MONTHLY_INCOME = new BigDecimal("3000000");

  public void validate(User user, UserBiodata userBiodata, BigDecimal loanAmount) {
    validateOverdueDays(user);
    validateDebtToIncomeRatio(userBiodata, loanAmount);
    validateLoanHistory(user);
    validateMinimumIncome(userBiodata);
  }

  private void validateOverdueDays(User user) {
    if (user.getTotalOverdueDays() > MAX_OVERDUE_DAYS) {
      throw new IllegalStateException(
          String.format(
              "Risk check failed: User has excessive overdue days (%d). Loan application rejected.",
              user.getTotalOverdueDays()));
    }
  }

  private void validateDebtToIncomeRatio(UserBiodata userBiodata, BigDecimal loanAmount) {
    if (userBiodata.getMonthlyIncome() != null
        && loanAmount.doubleValue()
            > userBiodata.getMonthlyIncome().doubleValue() * MAX_DEBT_TO_INCOME_RATIO) {
      throw new IllegalStateException(
          "Risk check failed: Loan amount exceeds 10x monthly income. Loan application rejected.");
    }
  }

  private void validateLoanHistory(User user) {
    if (user.getLoansCompleted() > MAX_COMPLETED_LOANS_FOR_RISK
        && user.getTotalOverdueDays() > MAX_OVERDUE_FOR_HIGH_LOAN_HISTORY) {
      throw new IllegalStateException(
          "Risk check failed: User has high loan history with overdue records. Loan application rejected.");
    }
  }

  private void validateMinimumIncome(UserBiodata userBiodata) {
    if (userBiodata.getMonthlyIncome() != null
        && userBiodata.getMonthlyIncome().compareTo(MIN_MONTHLY_INCOME) < 0) {
      throw new IllegalStateException(
          "Risk check failed: Monthly income below minimum requirement (Rp 3,000,000). Loan application rejected.");
    }
  }
}
