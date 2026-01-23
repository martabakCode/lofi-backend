package com.lofi.lofiapps.service;

import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.LoanStatus;
import org.springframework.stereotype.Service;

@Service
public class LoanActionValidator {
  public void validate(Loan loan, String action) {
    // Basic state machine validation
    // Action names: "review", "approve", "reject", "disburse", "submit"

    switch (action.toLowerCase()) {
      case "submit":
        if (loan.getLoanStatus() != LoanStatus.DRAFT) {
          throw new IllegalStateException("Only DRAFT loans can be submitted.");
        }
        break;
      case "review":
        if (loan.getLoanStatus() != LoanStatus.SUBMITTED) {
          throw new IllegalStateException("Only SUBMITTED loans can be reviewed.");
        }
        break;
      case "approve":
        if (loan.getLoanStatus() != LoanStatus.REVIEWED) {
          throw new IllegalStateException("Only REVIEWED loans can be approved.");
        }
        break;
      case "reject":
        // Reject can happen at SUBMITTED (by Marketing?) or REVIEWED (by Manager)
        if (loan.getLoanStatus() != LoanStatus.SUBMITTED
            && loan.getLoanStatus() != LoanStatus.REVIEWED) {
          // Maybe also after APPROVED? Assume not for now.
          throw new IllegalStateException("Loan cannot be rejected at this stage.");
        }
        break;
      case "disburse":
        if (loan.getLoanStatus() != LoanStatus.APPROVED) {
          throw new IllegalStateException("Only APPROVED loans can be disbursed.");
        }
        break;
      default:
        // No-op or error
    }
  }
}
